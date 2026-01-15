package com.laitsneo.mipPay.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class CustomBadCredentialsException extends BadCredentialsException {

    public CustomBadCredentialsException(){
        super ("Invalid Credentialds !!");
    }

    public CustomBadCredentialsException(String msg) {
        super(msg);
    }
}
