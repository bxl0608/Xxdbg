package com.bxlong.xxdbg.memory;

import com.bxlong.xxdbg.android.emulater.IEmulate;
import com.bxlong.xxdbg.android.linker.Linker;

import java.io.File;

public final class Memory {
    private IEmulate emulate;
    private Linker linker;

    public Memory(IEmulate emulate){
        this.emulate = emulate;
        linker = new Linker(emulate);
    }

    public void loadLibrary(File elf){
        linker.loadLibrary(elf);
    }
}
