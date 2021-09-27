package com.bxlong.xxdbg;

import com.bxlong.xxdbg.android.emulater.AndroidEmulate;
import com.bxlong.xxdbg.memory.Memory;
import com.bxlong.xxdbg.utils.FileHelper;

public class AndroidEmulateTest {
    public static void main(String[] args) {
        AndroidEmulate emulate = new AndroidEmulate.Builder()
                .for32Bit()
                .setBackendType(AndroidEmulate.BackendType.Unicorn)
                .build();
        //System.out.println(emulate);
        Memory memory = emulate.getMemory();
        memory.loadLibrary(FileHelper.getResourceFile(AndroidEmulateTest.class, "example/libnative-lib.so"));
    }
}
