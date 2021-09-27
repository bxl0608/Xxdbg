package com.bxlong.xxdbg.memory;

import com.bxlong.xxdbg.android.emulater.IEmulate;

public class Pointer {

    private IEmulate emulate;
    private long peer;
    private long pointerSize;

    public Pointer(IEmulate emulate, long peer) {
        this.emulate = emulate;
        this.peer = peer;
    }
}
