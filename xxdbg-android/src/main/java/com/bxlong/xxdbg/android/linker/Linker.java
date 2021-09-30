package com.bxlong.xxdbg.android.linker;

import com.bxlong.elf.*;
import com.bxlong.xxdbg.android.emulater.IEmulate;
import com.bxlong.xxdbg.android.module.ElfModule;
import com.bxlong.xxdbg.backend.arm.Arm;
import com.bxlong.xxdbg.memory.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static unicorn.UnicornConst.*;

/**
 * Linker
 * http://androidxref.com/4.4.4_r1/xref/bionic/linker/linker_phdr.cpp
 * http://androidxref.com/4.4.4_r1/xref/bionic/linker/linker.cpp
 */
public class Linker {
    private static final Logger logger = LoggerFactory.getLogger(Linker.class);
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

        List<ElfSegment> loadSegments = elf.getLoadSegment();
        if (loadSegments == null) {
            throw new LinkerException(elfName + " hasn't load segment!");
        }
        for (ElfSegment load : loadSegments) {
            found_pt_load = true;
            if (load.virtual_address < min_vaddr) {
                min_vaddr = load.virtual_address;
            }

            if (load.virtual_address + load.mem_size > max_vaddr) {
                max_vaddr = load.virtual_address + load.mem_size;
            }
        }

        if (!found_pt_load) {
            min_vaddr = 0x00000000;
        }
        min_vaddr = Arm.PAGE_START(min_vaddr);
        max_vaddr = Arm.PAGE_END(max_vaddr);
        long load_size_ = max_vaddr - min_vaddr;
        if (load_size_ <= 0) {
            throw new LinkerException(elfName + " has no loadable segments");
        }

        long load_start_ = mmap(-1, load_size_, UC_PROT_ALL);

        long load_bias = load_start_ - min_vaddr;

        //LoadSegments
        for (ElfSegment load : loadSegments) {
            //Segment addresses in memory.
            long seg_start = load.virtual_address + load_bias;
            long seg_end = seg_start + load.mem_size;

            //Segment addresses in page.
            long seg_page_start = Arm.PAGE_START(seg_start);
            long seg_page_end = Arm.PAGE_END(seg_end);

            long seg_file_end = seg_start + load.file_size;

            //File offset
            long file_start = load.offset;
            long file_end = file_start + load.file_size;

            long file_page_start = Arm.PAGE_START(file_start);
            long file_length = file_end - file_page_start;

            if (file_length != 0) {
                //mprotect(seg_page_start, file_length, load.flags);
                mwrite(seg_page_start, elf.getBytes(file_page_start, (int) file_length));
            }

            // 如果该段可写，且文件末跟页末尾有空余，需0填充
            if ((load.flags & UC_PROT_WRITE) != 0 && Arm.PAGE_OFFSET(seg_file_end) > 0) {
                byte[] zeros = new byte[(int) (Arm.PAGE_SIZE - Arm.PAGE_OFFSET(seg_file_end))];
                emulate.getBackend().mem_write(seg_file_end, zeros);
            }

            seg_file_end = Arm.PAGE_END(seg_file_end);
            //如果该段的mem_size > file_size 且超过一个页，在android源码中，它将多出的页进行匿名映射，防止出现Bus error的情况
            if (seg_page_end - seg_file_end > 0) {
                byte[] zeros = new byte[(int) (seg_page_end - seg_file_end)];
                mwrite(Arm.PAGE_END(seg_file_end), zeros);
            }
        }

        //Segment Loaded

        ElfModule elfModule = new ElfModule();
        elfModule.setBase(load_start_);
        elfModule.setLoad_bias_(load_bias);
        elfModule.setName(elfName);
        elfModule.setSize(load_size_);
        elfModule.setElfFile(elf);
        loadedModules.add(elfModule);

        debug("[%s] is load complex, load_start: 0x%x, load_bias:0x%x, size:%d",
                elfName, load_start_, load_bias, load_size_);

        return elfModule;
    }

    /**
     * 链接, 加载需要的Library
     *
     * @param elfModule
     * @return
     */
    private boolean link_library(ElfModule elfModule) {
        //TODO Relocation
        ElfFile elf = elfModule.getElfFile();
//        long base = elfModule.getLoad_bias_();
//        int phnum = elf.num_ph;
//        int dynamic_count = 0;
//        int dynamic_flag = 0;
        ElfDynamicStructure dynamicStructure = elf.getDynamicSegment().getDynamicStructure();
        if (dynamicStructure == null) {
            throw new LinkerException(elfModule.getName() + " can't find the dynamic structure");
        }
//        for (int i = 0; i < phnum; i++) {
//            ElfSegment programHeader = elf.getProgramHeader(i);
//            if (programHeader.type == PT_DYNAMIC) {
//                dynamic_count = (int) (programHeader.mem_size / 8);
//                dynamic_flag = programHeader.flags;
//                dynamicSegment = programHeader.
//                break;
//            }
//        }
        //int dt_needed = 0;

        //ElfDynamicSection dynamicSection = elf.getDynamicSection();
        List<ElfModule> needs = new ArrayList<>();
        for (String need : dynamicStructure.getNeededLibraries()) {
            debug("[%s] need %s library.", elfModule.getName(), need);
            ElfModule neededLibrary = find_library(null, need);
            needs.add(neededLibrary);
        }

        if (!relocation_library(elfModule, needs)) {
            debug("[%s] relocation error!");
            return false;
        }
        elfModule.setLinked(true);
        return true;
    }

    /**
     * 重定位
     *
     * @param elfModule
     * @return
     */
    private boolean relocation_library(ElfModule elfModule, List<ElfModule> needs) {
        ElfFile elf = elfModule.getElfFile();
        long base = elfModule.getLoad_bias_();

        ElfDynamicStructure dynamicStructure = elf.getDynamicSegment().getDynamicStructure();

        for (MemoizedObject<ElfRelocation> rel_item : dynamicStructure.getRelocations()) {
            ElfRelocation rel = rel_item.getValue();
            int type = rel.type();
            int sym = rel.sym();
            long reloc = rel.offset() + base;

            // R_*_NONE
            if (type == 0) {
                continue;
            }
            ElfSymbol symbol;
            long symbol_addr = 0;
            // 如果有符号，先找符号
            if (sym != 0) {
                String sym_name = rel.symbol().getName();
                ElfSymbol s = do_look_up(elfModule, sym_name, needs);
                if (s == null) {
                    //如果没有找到外部符号，只允许虚引用存在
                    if (rel.symbol().getBinding() != 2) { // #define STB_WEAK 2
                        //throw new LinkerException(sym_name + "can not found!");
                    }
                    switch (type) {
                        case IEmulate.R_ARM_JUMP_SLOT:
                        case IEmulate.R_ARM_GLOB_DAT:
                        case IEmulate.R_ARM_ABS32:
                        case IEmulate.R_ARM_RELATIVE:
                            continue;
                    }
                    debug("symbol name : [%s] can not resolve!", sym_name);
                    throw new LinkerException(sym_name + "can not found!");
                } else {
                    //符号找到了
                    /**
                     * sym_addr = static_cast<Elf32_Addr>(s->st_value + lsi->load_bias);
                     */
                    symbol = s;
                    symbol_addr = s.value + s.getLoad_bias_();
                }
            } else {
                symbol = null;
            }

            //开始真正重定位
            Pointer relocate_p = new Pointer(emulate, reloc);
            Pointer symbol_addr_p = new Pointer(emulate, symbol_addr);

            switch (type) {
                case IEmulate.R_ARM_JUMP_SLOT:
                case IEmulate.R_ARM_GLOB_DAT:

                    relocate_p.setPointer(0, symbol_addr_p);
                    break;
                case IEmulate.R_ARM_ABS32:
                    relocate_p.setPointer(reloc, symbol_addr_p);
                    break;
                case IEmulate.R_ARM_REL32:
                    relocate_p.setPointer(reloc - rel.offset(), symbol_addr_p);
                    break;
                case IEmulate.R_ARM_RELATIVE:
                    relocate_p.setPointer(0, relocate_p);
                    break;
                case IEmulate.R_ARM_COPY:
                default:
                    throw new LinkerException("the type: " + type + "hasn't support");
                    //break;
            }
        }
        return true;
    }

    /**
     * 查找符号
     *
     * @param sym_name
     * @param needs
     */
    private ElfSymbol do_look_up(ElfModule elfModule, String sym_name, List<ElfModule> needs) {

        //long hash = nameHash(sym_name);
        for (ElfModule need : needs) {
            ElfFile elf = need.getElfFile();
            ElfSymbolStructure value = elf.getDynamicSegment().getDynamicStructure().getSymbolTable().getValue();
            ElfSymbol symbol = value.getELFSymbolByName(sym_name);
            if (symbol != null) {
                symbol.setLoad_bias_(need.getLoad_bias_());
                return symbol;
            }
        }
        //debug("[%s] hash is : %d", sym_name, hash);
        return null;
    }

    private long mmap(long address, long size, int perms/*, byte[] value*/) {

        if (address < 0) {
            address = BASE;
        }
        try {
            size = Arm.PAGE_END(size);
            emulate.getBackend().mem_map(address, size, perms);
            BASE += size;
        } catch (Exception e) {
            debug("addr: 0x%x, map failed!", address);
            throw new LinkerException("memory failed! address: 0x" + Long.toHexString(address));
        }

        return address;
    }

    private void mwrite(long address, byte[] data) {
        if (data != null) {
            emulate.getBackend().mem_write(address, data);
        }
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
        for (ElfModule module : loadedModules) {
            if (module.getName().equals(name)) {
                return module;
            }
        }
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
                    debug("[%s] is also loaded!", name);
                    return loaded_library;
                } else {
                    // 未链接，逻辑异常
                    throw new LinkerException("linker error!");
                }
            } else {
                // 未加载，执行加载流程
                debug("[%s] prepare to load.", name);
                ElfModule loadModule = load_library(elfFile);
                if (loadModule == null) {
                    throw new LinkerException(elfName + " load error!");
                }
                // 进行链接/重定位
                debug("[%s] prepare to link and relocation.", name);
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
        if (lib == null) {
            throw new LinkerException(String.format("the [%s] file can't find", elfName));
        }
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
    public ElfModule dl_open(File elfFile, boolean isCallConstructors) {
        ElfModule library = find_library(elfFile, null);
        if (isCallConstructors && !library.isInit()) {
            call_constructors(library);
        }
        return library;
    }

    private void debug(String format, Object... args) {
        logger.debug(String.format(format, args));
    }
}
