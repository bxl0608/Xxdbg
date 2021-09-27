package com.bxlong.xxdbg.backend.arm;

public class Arm {
    private final static long PAGE_SHIFT =  12;
    public final static long PAGE_SIZE =  (1 << PAGE_SHIFT);
    private final static long PAGE_MASK = (-PAGE_SIZE);

    public static long PAGE_START(long x){
        return (x) & PAGE_MASK;
    }

    public static long PAGE_OFFSET(long x){
        return ((x) & ~PAGE_MASK);
    }

    public static long PAGE_END(long x){
        return PAGE_START((x) + (PAGE_SIZE-1));
    }
}
