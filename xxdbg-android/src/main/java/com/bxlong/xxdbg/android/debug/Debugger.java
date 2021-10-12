package com.bxlong.xxdbg.android.debug;

import unicorn.BlockHook;
import unicorn.DebugHook;

import java.io.Closeable;

public interface Debugger extends Breaker, DebugHook, BlockHook, Closeable {


}
