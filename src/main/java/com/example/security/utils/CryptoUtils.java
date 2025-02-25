package com.example.security.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class CryptoUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String SECRET_KEY = "YourSecretKey123"; // Should be 16 bytes for AES-128
    private static final int GCM_IV_LENGTH = 12; // GCM recommended IV length
    private static final int GCM_TAG_LENGTH = 128; // GCM Tag length in bits

    private CryptoUtils(){}

    private static SecretKey getSecretKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
    }

    private static byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static String encrypt(String data) {
        try {
            if (data == null) return null;

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = generateIv();
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), gcmParameterSpec);

            byte[] encryptedValue = cipher.doFinal(data.getBytes());
            byte[] ivAndEncryptedValue = new byte[iv.length + encryptedValue.length];

            System.arraycopy(iv, 0, ivAndEncryptedValue, 0, iv.length);
            System.arraycopy(encryptedValue, 0, ivAndEncryptedValue, iv.length, encryptedValue.length);

            return Base64.getEncoder().encodeToString(ivAndEncryptedValue);
        } catch (Exception e) {
            log.error("Exception in encryption: ", e);
            throw new CustomException(e.getMessage(), 500);
        }
    }

    public static String decrypt(String text){
        try {
            if (text == null) return null;

            byte[] ivAndEncryptedValue = Base64.getDecoder().decode(text);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedValue = new byte[ivAndEncryptedValue.length - GCM_IV_LENGTH];

            System.arraycopy(ivAndEncryptedValue, 0, iv, 0, iv.length);
            System.arraycopy(ivAndEncryptedValue, iv.length, encryptedValue, 0, encryptedValue.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), gcmParameterSpec);

            byte[] decryptedValue = cipher.doFinal(encryptedValue);
            return new String(decryptedValue);
        } catch (Exception e) {
            log.error("Exception in decryption: ", e);
            throw new CustomException(e.getMessage(), 500);
        }
    }
}