package com.bxlong.xxdbg.android.linker;

import com.bxlong.xxdbg.android.emulater.IEmulate;
import com.bxlong.xxdbg.android.module.ElfModule;
import com.bxlong.xxdbg.backend.IBackend;
import com.bxlong.xxdbg.backend.arm.Arm;
import com.sun.org.apache.bcel.internal.generic.ARETURN;
import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSegment;

import javax.naming.LinkException;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

import static net.fornwall.jelf.ElfSegment.*;
import static unicorn.UnicornConst.*;

/**
 * Linker
 * http://androidxref.com/4.4.4_r1/xref/bionic/linker/linker_phdr.cpp
 * http://androidxref.com/4.4.4_r1/xref/bionic/linker/linker.cpp
 */
public class Linker {
    private static long BASE = 0x40000000;
    IEmulate emulate;
    private List<ElfModule> loadedModules = new LinkedList<ElfModule>();

    public Linker(IEmulate emulate) {
        this.emulate = emulate;
    }

    /**
     * 加载elf文件到内存
     *
     * @param elfFile elf文件
     */
    public ElfModule load_library(File elfFile) {
        String elfName = elfFile.getName();
        InputStream elfIs = open_library(elfFile);

        ElfFile elf;
        try {
            elf = ElfFile.from(elfIs);
        } catch (Exception e) {
            throw new LinkerException(elfName + " file is error!");
        }
        //ReserveAddressSpace()
        long min_vaddr = Long.MAX_VALUE;
        long max_vaddr = 0x00000000;
        boolean found_pt_load = false;
        for (int i = 0; i < elf.num_ph; i++) {
            ElfSegment programHeader = elf.getProgramHeader(i);
            if (programHeader.type != PT_LOAD) {
                continue;
            }
            found_pt_load = true;
            if (programHeader.virtual_address < min_vaddr) {
                min_vaddr = programHeader.virtual_address;
            }

            if (programHeader.virtual_address + programHeader.mem_size > max_vaddr) {
                max_vaddr = programHeader.virtual_address + programHeader.mem_size;
            }
        }
        if (!found_pt_load) {
            min_vaddr = 0x00000000;
        }
        min_vaddr = Arm.PAGE_START(min_vaddr);
        max_vaddr = Arm.PAGE_END(max_vaddr);
        long load_size_ = max_vaddr - min_vaddr;
        if (load_size_ == 0) {
            throw new LinkerException(elfName + " has no loadable segments");
        }

        long load_start_ = mmap(BASE, load_size_, UC_PROT_NONE, null);
        BASE += load_size_;
        long load_bias = load_start_ - min_vaddr;

        //LoadSegments
        for (int i = 0; i < elf.num_ph; i++) {
            ElfSegment programHeader = elf.getProgramHeader(i);
            if (programHeader.type != PT_LOAD) {
                continue;
            }
            //Segment addresses in memory.
            long seg_start = programHeader.virtual_address + load_bias;
            long seg_end = seg_start + programHeader.mem_size;

            //Segment addresses in page.
            long seg_page_start = Arm.PAGE_START(seg_start);
            long seg_page_end = Arm.PAGE_END(seg_end);

            long seg_file_end = seg_start + programHeader.file_size;

            //File offset
            long file_start = programHeader.offset;
            long file_end = file_start + programHeader.file_size;

            long file_page_start = Arm.PAGE_START(file_start);
            long file_length = file_end - file_page_start;

            if (file_length != 0) {
                mmap(seg_page_start, file_length, programHeader.flags, elf.getBytes(file_page_start, (int) file_length));
            }

            if ((programHeader.flags & UC_PROT_WRITE) != 0 && Arm.PAGE_OFFSET(seg_file_end) > 0) {
                byte[] zeros = new byte[(int) (Arm.PAGE_SIZE - Arm.PAGE_OFFSET(seg_file_end))];
                emulate.getBackend().mem_write(seg_file_end, zeros);
            }
            seg_file_end = Arm.PAGE_END(seg_file_end);

            if (seg_page_end - seg_file_end > 0) {
                byte[] zeros = new byte[(int) (seg_page_end - seg_file_end)];
                mmap(BASE, seg_page_end - seg_file_end, UC_PROT_NONE, zeros);
            }
        }

        //Segment Loaded

        ElfModule elfModule = new ElfModule();
        elfModule.setBase(load_start_);
        elfModule.setLoad_bias_(load_bias);
        elfModule.setName(elfName);
        elfModule.setSize(load_size_);
        loadedModules.add(elfModule);

        return elfModule;
    }

    /**
     * 重定位
     *
     * @param elfModule
     * @return
     */
    private boolean link_library(ElfModule elfModule) {
        return false;
    }


    private long mmap(long address, long size, int perms, byte[] value) {
        if (address >= BASE) {
            emulate.getBackend().mem_map(address, size, perms);
        }
        if (value != null) {
            emulate.getBackend().mem_write(address, value);
        }
        return address;
    }

    /**
     * 打开库文件
     *
     * @param elfFile elf文件
     * @return stream
     */
    private InputStream open_library(File elfFile) {
        if (!elfFile.canRead()) {
            throw new LinkerException(elfFile.getPath() + " can not read!");
        }
        try {
            return new FileInputStream(elfFile);
        } catch (FileNotFoundException e) {
            throw new LinkerException(elfFile.getPath() + " file not found!");
        }
    }

    /**
     * 查找已加载的模块
     *
     * @param name
     * @return
     */
    private ElfModule find_loaded_library(String name) {
        return null;
    }

    /**
     * 如elfName不为空，优先加载elfName指定文件
     *
     * @param elfFile
     * @param elfName
     * @return
     */
    private ElfModule find_library_internal(File elfFile, String elfName) {
        if (elfName != null) {
            elfFile = emulate.getSystemLibrary(elfName);
        }
        if (elfFile != null) {
            String name = elfFile.getName();
            ElfModule loaded_library = find_loaded_library(name);
            if (loaded_library != null) {
                if (loaded_library.isLinked()) {
                    // 已经加载且链接过了，直接返回
                    return loaded_library;
                } else {
                    // 未链接，逻辑异常
                    throw new LinkerException("linker error!");
                }
            } else {
                // 未加载，执行加载流程
                ElfModule loadModule = load_library(elfFile);
                if (loadModule == null) {
                    throw new LinkerException(elfName + " load error!");
                }
                // 进行重定位
                if (!link_library(loadModule)) {
                    throw new LinkerException("linker error!");
                }
                return loadModule;
            }
        }
        return null;
    }


    private ElfModule find_library(File elfFile, String elfName) {
        ElfModule lib = find_library_internal(elfFile, elfName);
        lib.setRef(lib.getRef() + 1);
        return lib;
    }

    /**
     * 执行初始化函数，且不对外开放
     *
     * @param module
     * @return
     */
    private boolean call_constructors(ElfModule module) {
        //TODO CallConstructors
        return false;
    }

    /**
     * 加载库文件接口
     *
     * @param elfFile
     * @param isCallConstructors 如需执行初始化函数，需要指定为true
     * @return
     */
    public ElfModule do_dl_open(File elfFile, boolean isCallConstructors) {
        ElfModule library = find_library(elfFile, null);
        if (isCallConstructors && !library.isInit()) {
            call_constructors(library);
        }
        return library;
    }
}
