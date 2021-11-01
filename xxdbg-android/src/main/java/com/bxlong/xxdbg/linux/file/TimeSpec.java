package com.bxlong.xxdbg.linux.file;

import com.bxlong.xxdbg.memory.BaseStructure;
import com.bxlong.xxdbg.memory.Pointer;

import java.util.Arrays;
import java.util.List;

public class TimeSpec extends BaseStructure {
    public TimeSpec(Pointer pointer){
        super(pointer);
    }
    public int tv_sec; // unsigned long
    public int tv_nsec; // long

    @Override
    public List<String> getFieldOrder() {
        return Arrays.asList("tv_sec", "tv_nsec");
    }
}