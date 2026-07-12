package io.dbflow.common.security;

public interface StringEncryptor {
    String encrypt(String plainText);

    String decrypt(String encryptedText);

    boolean isEncrypted(String value);
}
