package com.laitsneo.whitelbl.helper;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class AESEncryptor {

    private static final String ALGORITHM = "AES";
    private static final byte[] keyValue = "1Hbfh667adfDEJ78".getBytes();

    public static String encrypt(String mpin) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encValue = cipher.doFinal(mpin.getBytes());
        byte[] encryptedByteValue = Base64.getEncoder().encode(encValue);
        System.out.println("Encrypted Value :: " + new String(encryptedByteValue));
        return new String(encryptedByteValue);
    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGORITHM);
        return key;
    }

    public static String decrypt(String encryptedValue) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedValue.getBytes());
        byte[] enctVal = cipher.doFinal(decodedBytes);
        System.out.println("Decrypted Value :: " + new String(enctVal));
        return new String(enctVal);
    }

    public static String encryptKey(String plainText) throws Exception {
        // Generate AES key (256 bits) and IV (12 bytes)
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey aesKey = keyGen.generateKey();

        byte[] iv = new byte[12];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // Encrypt plaintext using AES/GCM/NoPadding
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);

        byte[] cipherText = aesCipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        byte[] authTag = new byte[16];
        System.arraycopy(cipherText, cipherText.length - 16, authTag, 0, 16);

        byte[] encryptedPayload = cipherText; // ciphertext + tag (GCM appends tag automatically)

        // Encrypt AES key and IV using RSA (PKCS#1 v1.5)
        PublicKey publicKey = loadPublicKey("-----BEGIN PUBLIC KEY----- MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2Sm8AO8khXE+odCVAWfZ vRZvp0KklXt28CEW83NiEVjGC/pKOYlJd//K+mekQMz0aSH5t8yAuHkcykmGb88+ +Niz09PgKIwa7ywPLVTeHZV4ZKoEHOZH72kTOuji5U32W084UKRZKwZ7BCqLUhQF 9U+XPpkxPYoCLOJnxhMTG00ugqRd3gNI1cGzNw4obUJsjg8mpXnD+XqKcgZi59GE qt3j0hsdfIgmu7q9Er2pk0hXedxwj1OAZPrpS4Ni4ZYPi/yZFQEPwERlE3zfhJy7 mm8q4OI677idKYGMbu6gl4DCPnyUAgnE0tvBTvVM5k8JGOND45v3QM2aJZGtGMeJ 8QIDAQAB -----END PUBLIC KEY-----");

        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedKey = rsaCipher.doFinal(aesKey.getEncoded());
        byte[] encryptedIv = rsaCipher.doFinal(iv);

        // Encode to Base64 and combine
        String dataB64 = Base64.getEncoder().encodeToString(encryptedPayload);
        String encryptedKeyB64 = Base64.getEncoder().encodeToString(encryptedKey);
        String encryptedIvB64 = Base64.getEncoder().encodeToString(encryptedIv);

        return dataB64 + "^" + encryptedKeyB64 + "^" + encryptedIvB64;
    }

    private static PublicKey loadPublicKey(String pem) throws Exception {
        String cleanPem = pem.replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        System.out.println("cleanPem: "+cleanPem);
        byte[] keyBytes = Base64.getDecoder().decode(cleanPem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    // Method to encrypt the data
    public static String encryptOtp(String data) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    // Method to decrypt the data
    public static String decryptOtp(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] originalData = cipher.doFinal(decodedData);
        return new String(originalData);
    }
}
