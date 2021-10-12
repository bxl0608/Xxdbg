package com.bxlong.xxdbg.memory;

import com.bxlong.xxdbg.android.emulater.AndroidEmulate;
import com.bxlong.xxdbg.android.emulater.IEmulate;
import com.bxlong.xxdbg.android.linker.Linker;
import com.bxlong.xxdbg.android.module.ElfModule;
import com.bxlong.xxdbg.backend.arm.ARM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unicorn.ArmConst;
import unicorn.Unicorn;
import unicorn.UnicornConst;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import static com.bxlong.xxdbg.android.emulater.IEmulate.LR;
import static java.awt.GridBagConstraints.PAGE_START;

public final class Memory {
    private static final Logger logger = LoggerFactory.getLogger(Memory.class);
    private final IEmulate emulate;
    private final Linker linker;
    long STACK_BASE = 0xc0000000L;
    int STACK_SIZE_OF_PAGE = 256; // 1024k
    private static long MMAP_BASE = 0x40000000;
    protected final Map<Long, MemoryMap> memoryMap = new TreeMap<>();

    public void init() {
        // map返回地址的内存
        emulate.getBackend().mem_map(LR, 0x1000, Unicorn.UC_PROT_ALL);

        //设置SP
        emulate.getBackend().reg_write(ArmConst.UC_ARM_REG_SP, STACK_BASE);
        long stackSize = STACK_SIZE_OF_PAGE * 4096;
        emulate.getBackend().mem_map(STACK_BASE - stackSize, stackSize, UnicornConst.UC_PROT_READ | UnicornConst.UC_PROT_WRITE);

    }

    public String getElfModuleNameByAddress(long address) {
        return linker.getElfModuleNameByAddress(address);
    }

    public ElfModule getElfModuleByAddress(long address) {
        return linker.getElfModuleByAddress(address);
    }

    public Memory(IEmulate emulate) {
        this.emulate = emulate;
        linker = new Linker(emulate);
    }

    public ElfModule loadLibrary(File elf, boolean isCallConstructors) {
        return linker.dl_open(elf, isCallConstructors);
    }

    public static final int MAP_FIXED = 0x10;
    public static final int MAP_ANONYMOUS = 0x20;

    public long mmap(long start, int length, int prot, int flags, int fd, int offset) {
        int alignedSize = (int) ARM.PAGE_END(length);
        //是否匿名映射
        boolean isAnonymous = ((flags & MAP_ANONYMOUS) != 0) || (start == 0 && fd <= 0 && offset == 0);
        if ((flags & MAP_FIXED) != 0 && isAnonymous) {
//            if (log.isDebugEnabled()) {
//                log.debug("mmap2 MAP_FIXED start=0x" + Long.toHexString(start) + ", length=" + length + ", prot=" + prot);
//            }
//
//            munmap(start, length);
            emulate.getBackend().mem_map(start, alignedSize, prot);
//            if (memoryMap.put(start, new MemoryMap(start, aligned, prot)) != null) {
//                log.warn("mmap2 replace exists memory map: start=" + Long.toHexString(start));
//            }
//            return start;
        }
        if (isAnonymous) {
            long addr = allocateMapAddress(0, alignedSize);
            debug("mmap addr=0x%x, mmapBaseAddress=0x%x, start=0x%x, fd=%d, offset=0x%x, aligned=0x%x", addr, MMAP_BASE, start, fd, offset, alignedSize);
            emulate.getBackend().mem_map(addr, alignedSize, prot);
            if (memoryMap.put(addr, new MemoryMap(addr, alignedSize, prot)) != null) {
                debug("mmap replace exists memory map addr=0x%x", addr);
            }
            return addr;
        }

        throw new UnsupportedOperationException("can not resolve the [mmap]");
    }

    public final int mprotect(long address, int length, int prot) {
//        if (address % ARMEmulator.PAGE_ALIGN != 0) {
//            setErrno(UnixEmulator.EINVAL);
//            return -1;
//        }

        emulate.getBackend().mem_protect(address, length, prot);
        return 0;
    }


    private long allocateMapAddress(long mask, long length) {
        Map.Entry<Long, MemoryMap> lastEntry = null;
        for (Map.Entry<Long, MemoryMap> entry : memoryMap.entrySet()) {
            if (lastEntry == null) {
                lastEntry = entry;
            } else {
                MemoryMap map = lastEntry.getValue();
                long mmapAddress = map.base + map.size;
                if (mmapAddress + length < entry.getKey() && (mmapAddress & mask) == 0) {
                    return mmapAddress;
                } else {
                    lastEntry = entry;
                }
            }
        }
        if (lastEntry != null) {
            MemoryMap map = lastEntry.getValue();
            long mmapAddress = map.base + map.size;
            if (mmapAddress < MMAP_BASE) {
                //log.debug("allocateMapAddress mmapBaseAddress=0x" + Long.toHexString(mmapBaseAddress) + ", mmapAddress=0x" + Long.toHexString(mmapAddress));
                setMMapBaseAddress(mmapAddress);
            }
        }

        long addr = MMAP_BASE;
        while ((addr & mask) != 0) {
            addr += ARM.getPageAlign();
        }
        setMMapBaseAddress(addr + length);
        return addr;
    }

    private void setMMapBaseAddress(long addr) {
        MMAP_BASE = addr;
    }


    private void debug(String format, Object... args) {
        logger.debug(String.format(format, args));
    }
}
