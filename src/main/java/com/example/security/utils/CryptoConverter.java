package com.example.security.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Converter
@Slf4j
public class CryptoConverter implements AttributeConverter<String, String> {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final byte[] keyValue = new byte[]{'q', '^', 'q', 'T', 'v', '"', '@', 'd', 'f', 'N', 'V', '!', 'u', '0', 'c', '{'};

    @Override
    public String convertToDatabaseColumn(String s) {
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(s.getBytes());
            return Base64.getEncoder().encodeToString(encVal);
        } catch (Exception ex) {
            log.info("Exception encrypt :: {}", ex.getMessage());
            return null;
        }
    }

    @Override
    public String convertToEntityAttribute(String s) {
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decordedValue = Base64.getDecoder().decode(s);
            byte[] decValue = c.doFinal(decordedValue);
            return new String(decValue);
        } catch (Exception ex) {
            log.info("Exception encrypt :: {}", ex.getMessage());
            return null;
        }
    }

    private static Key generateKey()  {
        return new SecretKeySpec(keyValue, ALGO);
    }
}
