package io.dbflow.common.security;

import io.dbflow.common.Exception.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AesGcmStringEncryptor implements StringEncryptor {

    public static final String ENCRYPTED_PREFIX = "ENC:v1:";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int AUTHENTICATION_TAG_LENGTH_BITS = 128;

    private final EncryptionKeyProvider keyProvider;
    private final SecureRandom secureRandom;

    public AesGcmStringEncryptor(EncryptionKeyProvider keyProvider) {
        this(keyProvider, new SecureRandom());
    }

    AesGcmStringEncryptor(EncryptionKeyProvider keyProvider, SecureRandom secureRandom) {
        this.keyProvider = keyProvider;
        this.secureRandom = secureRandom;
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty() || isEncrypted(plainText)) {
            return plainText;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keyProvider.getKey(), new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH_BITS, iv));
            byte[] cipherTextWithTag = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] payload = ByteBuffer.allocate(iv.length + cipherTextWithTag.length)
                    .put(iv)
                    .put(cipherTextWithTag)
                    .array();

            return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new CryptoException(CryptoException.ENCRYPTION_FAILED, e);
        }
    }

    @Override
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty() || !isEncrypted(encryptedText)) {
            return encryptedText;
        }

        try {
            byte[] payload = Base64.getDecoder().decode(encryptedText.substring(ENCRYPTED_PREFIX.length()));
            if (payload.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid encrypted payload.");
            }

            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] cipherTextWithTag = new byte[payload.length - IV_LENGTH_BYTES];
            System.arraycopy(payload, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(payload, IV_LENGTH_BYTES, cipherTextWithTag, 0, cipherTextWithTag.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keyProvider.getKey(), new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH_BITS, iv));
            byte[] plainText = cipher.doFinal(cipherTextWithTag);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException(CryptoException.DECRYPTION_FAILED, e);
        }
    }

    @Override
    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENCRYPTED_PREFIX);
    }
}
