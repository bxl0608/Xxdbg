package com.bxlong.xxdbg.backend.arm;

public class ARM {
    public final static long MMAP2_SHIFT =  12;
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

    public static long getPageAlign(){
        return PAGE_SIZE;
    }

    private static final int ALIGN_SIZE_BASE = 0x10;

    public static int alignSize(int size) {
        return (int) alignSize(size, ALIGN_SIZE_BASE);
    }

    public static long alignSize(long size, long align) {
        return ((size - 1) / align + 1) * align;
    }
}
