package com.bxlong.xxdbg.android.debug;

import com.bxlong.xxdbg.memory.Pointer;

import java.awt.*;

public interface Breaker {

    void debug();

    void brk(Pointer pc, int svcNumber);

}