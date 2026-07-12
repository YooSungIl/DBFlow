package io.dbflow.common.security;

import javax.crypto.SecretKey;

public interface EncryptionKeyProvider {
    SecretKey getKey();
}
