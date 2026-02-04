package com.mippay.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

public class AES256EncryptionGSM {

    private static final int NONCE_LENGTH = 16; // 16 bytes nonce
    private static final int TAG_LENGTH_BIT = 128; // 16 bytes auth tag
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String encryptPayload(Map<String, Object> data) throws Exception {

        byte[] aesKey = Base64.getUrlDecoder().decode("cRxMwjXEVDV0jgKLuTG4ePQRZG8YDReU7K7f1b3T9Zk=");

        byte[] jsonBytes = objectMapper.writeValueAsString(data)
                .getBytes(StandardCharsets.UTF_8);
        byte[] nonce = new byte[NONCE_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(nonce);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKey key = new SecretKeySpec(aesKey, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, nonce);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        byte[] cipherTextWithTag = cipher.doFinal(jsonBytes);
        // Java output = ciphertext + tag
        int cipherTextLength = cipherTextWithTag.length - 16;
        byte[] ciphertext = new byte[cipherTextLength];
        byte[] tag = new byte[16];
        System.arraycopy(cipherTextWithTag, 0, ciphertext, 0, cipherTextLength);
        System.arraycopy(cipherTextWithTag, cipherTextLength, tag, 0, 16);
        // Python-compatible format: nonce + tag + ciphertext
        byte[] encryptedBlob = new byte[nonce.length + tag.length + ciphertext.length];
        System.arraycopy(nonce, 0, encryptedBlob, 0, nonce.length);
        System.arraycopy(tag, 0, encryptedBlob, nonce.length, tag.length);
        System.arraycopy(ciphertext, 0, encryptedBlob, nonce.length + tag.length, ciphertext.length);
        return Base64.getEncoder().encodeToString(encryptedBlob);
    }
    /**
     * Decrypt Base64 AES-GCM payload back to Map
     */
    public static Map<String, Object> decryptPayload(String encryptedB64) throws Exception {

        byte[] aesKey = Base64.getUrlDecoder().decode("cRxMwjXEVDV0jgKLuTG4ePQRZG8YDReU7K7f1b3T9Zk=");
        byte[] encryptedBlob = Base64.getDecoder().decode(encryptedB64);
        byte[] nonce = new byte[16];
        byte[] tag = new byte[16];
        byte[] ciphertext = new byte[encryptedBlob.length - 32];
        System.arraycopy(encryptedBlob, 0, nonce, 0, 16);
        System.arraycopy(encryptedBlob, 16, tag, 0, 16);
        System.arraycopy(encryptedBlob, 32, ciphertext, 0, ciphertext.length);
        // Java expects ciphertext + tag
        byte[] cipherTextWithTag = new byte[ciphertext.length + tag.length];
        System.arraycopy(ciphertext, 0, cipherTextWithTag, 0, ciphertext.length);
        System.arraycopy(tag, 0, cipherTextWithTag, ciphertext.length, tag.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKey key = new SecretKeySpec(aesKey, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, nonce);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        byte[] plainText = cipher.doFinal(cipherTextWithTag);
        return objectMapper.readValue(
                plainText,
                new TypeReference<Map<String, Object>>() {}
        );
    }
}
