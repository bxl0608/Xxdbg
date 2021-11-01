package com.bxlong.xxdbg.linux;

public class SysCallException extends RuntimeException{
    public SysCallException(String message) {
        super(message);
    }
}
