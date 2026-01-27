package com.mippay.helper;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
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

    public static String base64encodedHeaders() {
        String input = "1e7173f309454ebebac3b2ec17699aef:d2bebe33d42642aeb7a0c78afab59ab1:M:408000147774040";
        String base64Encoded = Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
        System.out.println(base64Encoded);
        return base64Encoded;
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
