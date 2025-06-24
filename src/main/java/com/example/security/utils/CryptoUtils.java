package com.example.security.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
public class CryptoUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String SECRET_KEY = "YourSecretKey123"; // Should be 16 bytes for AES-128
    private static final int IV_LENGTH = 16; // GCM recommended IV length
    private static final int GCM_TAG_LENGTH = 128; // GCM Tag length in bits

    private CryptoUtils(){}

    private static SecretKey getSecretKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
    }

    private static byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public String generateBase64Secret(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(secretBytes);
        return Base64.getEncoder().encodeToString(secretBytes);
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
            byte[] iv = new byte[IV_LENGTH];
            byte[] encryptedValue = new byte[ivAndEncryptedValue.length - IV_LENGTH];

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

    public String encryptDataWithAESKey(String base64Secret, String data) throws Exception {

        SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(base64Secret), "AES");

        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[IV_LENGTH + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(encryptedData, 0, combined, IV_LENGTH, encryptedData.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public String decryptDataWithAESKey(String base64Secret, String encryptedData) throws Exception {
        SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(base64Secret), "AES");

        byte[] combined = Base64.getDecoder().decode(encryptedData);

        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

        byte[] ciphertext = new byte[combined.length - IV_LENGTH];
        System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    public boolean validateWithAES(String base64Secret, String encryptedData, String plainText) {
        try {
            String text = decryptDataWithAESKey(base64Secret, encryptedData);
            return org.apache.commons.lang3.StringUtils.equals(plainText, text);
        } catch (Exception e) {
            log.error("Exception in validation: {}", e.getMessage());
            return false;
        }
    }

    public static RSAPrivateKey privateKeyFromPem(String filename) throws Exception {
        String privateKeyPEM = Files.readString(Paths.get(filename))
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("\r\n", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("\n", "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = java.util.Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    public static PublicKey publicKeyFromPem(String filename) throws Exception {
        String publicKeyPEM = Files.readString(Paths.get(filename))
                .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("\r\n", "")
                .replace("-----END RSA PUBLIC KEY-----", "")
                .replace("\n", "")
                .replace("-----END PUBLIC KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }

    public static String generateSignature(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);

        signature.update(data.getBytes());

        byte[] signedData = signature.sign();
        return Base64.getEncoder().encodeToString(signedData);
    }

    public static boolean verifySignature(String data, String signatureStr, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);

        signature.update(data.getBytes());

        byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);
        return signature.verify(signatureBytes);
    }
}