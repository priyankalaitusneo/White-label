package com.laitsneo.mipPay.exception;

public class CustomBadRequestException extends RuntimeException{
    public CustomBadRequestException(){
        super("Invalid request !!");
    }

    public CustomBadRequestException(String message){
        super(message);
    }
}
