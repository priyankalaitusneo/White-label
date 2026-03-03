package com.mippay.exception;

public class CustomUnauthorizedException extends RuntimeException {

    public CustomUnauthorizedException(){
        super("Access denied !! Unauthorized");
    }

    public CustomUnauthorizedException(String message){
        super("Access denied !! "+message);
    }

}
