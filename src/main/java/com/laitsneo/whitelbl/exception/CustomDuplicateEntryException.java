package com.laitsneo.whitelbl.exception;

public class CustomDuplicateEntryException extends RuntimeException{

    public CustomDuplicateEntryException(){
        super("Duplicate Entry");
    }

    public CustomDuplicateEntryException(String message){
        super(message);
    }
}
