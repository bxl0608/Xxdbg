package com.bxlong.xxdbg.linux;

import com.bxlong.xxdbg.android.emulater.IEmulate;
import com.bxlong.xxdbg.backend.IBackend;
import com.bxlong.xxdbg.backend.arm.ARM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unicorn.ArmConst;
import unicorn.InterruptHook;
import unicorn.Unicorn;

import static com.bxlong.xxdbg.backend.arm.ARM.*;

public class SystemCallHandler implements InterruptHook {

    private static final Logger logger = LoggerFactory.getLogger(SystemCallHandler.class);

    @Override
    public void hook(Unicorn u, int intno, Object user) {
        IEmulate emulate = (IEmulate) user;
        IBackend backend = emulate.getBackend();
        Long R7 = (Long) u.reg_read(ArmConst.UC_ARM_REG_R7);
        int NR = R7.intValue();
        debug(">>> System call occur, intno: %d, NR: %d", intno, NR);

        switch (NR) {
            case 192:
                //mmap2
                u.reg_write(ArmConst.UC_ARM_REG_R0, mmap2(backend, emulate));
                break;
            case 220:
                //madvise
                u.reg_write(ArmConst.UC_ARM_REG_R0, madvise());
                break;
            case 125:
                //mprotect
                backend.reg_write(ArmConst.UC_ARM_REG_R0, mprotect(backend, emulate));
                break;
            default:
                debug("System call occur, can not resolve, NR:%d", NR);
                u.emu_stop();
                break;
        }

        //Test
        //u.emu_stop();
    }

    private int mprotect(IBackend backend, IEmulate emulate) {
        /**
         * https://man7.org/linux/man-pages/man2/mprotect.2.html
         *  int mprotect(void * addr , size_t len , int prot );
         */
        long address = backend.reg_read(ArmConst.UC_ARM_REG_R0).intValue() & 0xffffffffL;
        int length = backend.reg_read(ArmConst.UC_ARM_REG_R1).intValue();
        int prot = backend.reg_read(ArmConst.UC_ARM_REG_R2).intValue();

        long alignedAddress = PAGE_START(address);
        long alignedLength = PAGE_END(length);

        return emulate.getMemory().mprotect(alignedAddress, (int) alignedLength,prot);
    }

    private int madvise() {
        /**
         * https://man7.org/linux/man-pages/man2/madvise.2.html
         */
        return 0;
    }

    private int mmap2(IBackend backend, IEmulate emulate) {
        /**
         * https://man7.org/linux/man-pages/man2/mmap2.2.html
         * void *syscall(SYS_mmap2,
         * unsigned long addr,
         * unsigned long length,
         * unsigned long prot,
         * unsigned long flags,
         * unsigned long fd,
         * unsigned long pgoffset);
         */
        long addr = backend.reg_read(ArmConst.UC_ARM_REG_R0).intValue() & 0xffffffffL;
        int length = backend.reg_read(ArmConst.UC_ARM_REG_R1).intValue();
        int prot = backend.reg_read(ArmConst.UC_ARM_REG_R2).intValue();
        int flags = backend.reg_read(ArmConst.UC_ARM_REG_R3).intValue();
        int fd = backend.reg_read(ArmConst.UC_ARM_REG_R4).intValue();
        int pgoffset = backend.reg_read(ArmConst.UC_ARM_REG_R5).intValue() << ARM.MMAP2_SHIFT;

        debug("System call [mmap2] : addr: %d, length: %d, prot: %d, flags: %d, fd: %d, pgoffset: %d", addr, length, prot, flags, fd, pgoffset);

        long mmap = emulate.getMemory().mmap(addr, length, prot, flags, fd, pgoffset);
        return (int) mmap;
    }

    private void debug(String format, Object... args) {
        logger.debug(String.format(format, args));
    }
}
