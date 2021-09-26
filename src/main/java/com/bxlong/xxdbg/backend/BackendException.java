package com.bxlong.xxdbg.backend;

public class BackendException extends RuntimeException{
    public BackendException(){

    }

    public BackendException(String message){
        super(message);
    }
}
