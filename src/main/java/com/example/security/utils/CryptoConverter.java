package com.example.security.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
@Slf4j
public class CryptoConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String SECRET_KEY = "YourSecretKey123"; // Should be 16 bytes for AES-128
    private static final int GCM_IV_LENGTH = 12; // GCM recommended IV length
    private static final int GCM_TAG_LENGTH = 128; // GCM Tag length in bits

    private SecretKey getSecretKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
    }

    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            if (attribute == null) return null;

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = generateIv();
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), gcmParameterSpec);

            byte[] encryptedValue = cipher.doFinal(attribute.getBytes());
            byte[] ivAndEncryptedValue = new byte[iv.length + encryptedValue.length];

            System.arraycopy(iv, 0, ivAndEncryptedValue, 0, iv.length);
            System.arraycopy(encryptedValue, 0, ivAndEncryptedValue, iv.length, encryptedValue.length);

            return Base64.getEncoder().encodeToString(ivAndEncryptedValue);
        } catch (Exception e) {
            log.error("Exception in encryption: ", e);
            throw new CustomException(e.getMessage(), 500);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null) return null;

            byte[] ivAndEncryptedValue = Base64.getDecoder().decode(dbData);
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
