package com.laitsneo.whitelbl.exception;

public class CustomMethodArgumentNotValidException extends RuntimeException {

    public CustomMethodArgumentNotValidException(){
        super("Duplicate Entry");
    }

    public CustomMethodArgumentNotValidException(String message){
        super(message);
    }
}
