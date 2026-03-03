package com.laitsneo.whitelbl.helper;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

public class Generator {

    public static String generateRandomTranId(int len) {
        String chars = "0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public String generateAdminId(){
        String date = String.valueOf(new Date().getTime());
        return "PAY"+date+this.generateRandomString(4);
    }

    public static String generateRandomString(int length) {
        final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final SecureRandom RANDOM = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }


    public static String generateRandomNumber(int len) {
        String chars = "0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public String generateClientId() {
        String date = String.valueOf(new Date().getTime());
        return "PAYCL"+date+this.generateRandomString(4);
    }
}
