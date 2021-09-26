package com.bxlong.xxdbg;

import com.bxlong.xxdbg.android.emulater.AndroidEmulate;

public class AndroidEmulateTest {
    public static void main(String[] args) {
        AndroidEmulate emulate = new AndroidEmulate.Builder()
                .for32Bit()
                .setBackendType(AndroidEmulate.BackendType.Unicorn)
                .build();
        System.out.println(emulate);
    }
}
