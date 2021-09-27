package com.bxlong.xxdbg.android.emulater;


import com.bxlong.xxdbg.backend.IBackend;
import com.bxlong.xxdbg.memory.Memory;

import java.io.File;

public interface IEmulate {

    boolean is32Bit();

    boolean is64Bit();

    public IBackend getBackend();

    public Memory getMemory();

    public File getSystemLibrary(String name);

}
