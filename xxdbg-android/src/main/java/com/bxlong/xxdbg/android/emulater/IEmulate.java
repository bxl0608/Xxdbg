package com.bxlong.xxdbg.android.emulater;


import com.bxlong.xxdbg.backend.IBackend;
import com.bxlong.xxdbg.memory.Memory;

import java.io.File;

public interface IEmulate {

    int R_ARM_ABS32 = 2;
    int R_ARM_REL32 = 3;
    int R_ARM_COPY = 20;
    int R_ARM_GLOB_DAT = 21;
    int R_ARM_JUMP_SLOT = 22;
    int R_ARM_RELATIVE = 23;
    int R_ARM_IRELATIVE = 160;

    boolean is32Bit();

    boolean is64Bit();

    public IBackend getBackend();

    public Memory getMemory();

    public File getSystemLibrary(String name);

}
