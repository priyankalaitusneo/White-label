package com.laitsneo.mipPay.exception;

public class CustomInternalServerErrorException extends RuntimeException{

    public CustomInternalServerErrorException(){
        super("Something went wrong !!");
    }

    public CustomInternalServerErrorException(String message){
        super(message);
    }
}
