package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Converter
@Component
public class AttributeEncryptor implements AttributeConverter<String, String> {

    private static final Logger log = LoggerFactory.getLogger(AttributeEncryptor.class);
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    private final SecretKeySpec keySpec;
    private final IvParameterSpec ivSpec;

    public AttributeEncryptor(@Value("${app.security.encryption-key:ShopMartDefaultPIIEncryptionKey2026}") String secretKey) {
        try {
            // Generate a 256-bit key from the secret text using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            this.keySpec = new SecretKeySpec(keyBytes, "AES");

            // Use the first 16 bytes of SHA-256 of a salt as IV
            byte[] ivBytes = Arrays.copyOf(digest.digest("ShopMartIVSaltValue2026".getBytes(StandardCharsets.UTF_8)), 16);
            this.ivSpec = new IvParameterSpec(ivBytes);
        } catch (Exception e) {
            log.error("Failed to initialize AttributeEncryptor", e);
            throw new IllegalStateException("Failed to initialize AttributeEncryptor", e);
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return attribute;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Encryption failed for attribute", e);
            throw new IllegalArgumentException("Encryption failed", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("Decryption failed for dbData (returning plain text): {}", dbData);
            // Fallback for pre-existing unencrypted data
            return dbData;
        }
    }
}
