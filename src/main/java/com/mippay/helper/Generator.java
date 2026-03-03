package com.mippay.helper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
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
        String input = "0xd88318ead2d5ed439c45e83ef72d5d:0xe00ccf4967e49d448a89c2a95a2ab9:M:E1A4011A0AF7677";
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

    public static String generateBuckBoxToken(){
//        String secretKey = "vqBCBlPbo4RaEmn7ClQGkUuBoUViyhBAB2GHtCDpmJ6PCdOnMGX4";
        String secretKey = "13lrmA8Axm6vwnv1324js6ORrM3ASG3odji07TFumW51gaND2yhm";
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 7L * 24 * 60 * 60 * 1000); // 7 days

        String token = Jwts.builder()
                .claim("merchant_id", "BM705445")
                .claim("name", "KN ANGNAIKHAM")
                .claim("email", "meihitech@gmail.com")
                .setExpiration(expiryDate)
                .setIssuedAt(now)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        System.out.println(token);

        return token;
    }
}
