package com.bxlong.xxdbg.android.emulater;


import com.bxlong.xxdbg.backend.IBackend;
import com.bxlong.xxdbg.linux.file.IFileSystem;
import com.bxlong.xxdbg.memory.Memory;
import com.bxlong.xxdbg.memory.Pointer;

import java.io.File;

public interface IEmulate {

    int R_ARM_ABS32 = 2;
    int R_ARM_REL32 = 3;
    int R_ARM_COPY = 20;
    int R_ARM_GLOB_DAT = 21;
    int R_ARM_JUMP_SLOT = 22;
    int R_ARM_RELATIVE = 23;
    int R_ARM_IRELATIVE = 160;

    long LR = 0xffff0000L;

    boolean is32Bit();

    boolean is64Bit();

    IBackend getBackend();

    Memory getMemory();

    File getSystemLibrary(String name);

    Number eInit(long begin);

    void traceCode(long begin, long end);

    int getPointSize();

    String getProcessName();

    Pointer getErrnoPointer();

    IFileSystem getFileSystem();

    int getPageAlign();

    int getPid();
}
