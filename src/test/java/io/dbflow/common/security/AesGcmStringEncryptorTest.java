package io.dbflow.common.security;

import io.dbflow.common.Exception.CryptoException;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AesGcmStringEncryptorTest {

    private final StringEncryptor encryptor = encryptor((byte) 1);

    @Test
    void 암호화한_문자열을_복호화하면_원문과_동일하다() {
        String encrypted = encryptor.encrypt("secret-password");

        assertTrue(encrypted.startsWith(AesGcmStringEncryptor.ENCRYPTED_PREFIX));
        assertEquals("secret-password", encryptor.decrypt(encrypted));
    }

    @Test
    void 동일한_평문도_매번_다른_암호문을_생성한다() {
        assertNotEquals(encryptor.encrypt("same-password"), encryptor.encrypt("same-password"));
    }

    @Test
    void 잘못된_키로는_복호화할_수_없다() {
        String encrypted = encryptor.encrypt("secret-password");

        assertThrows(CryptoException.class, () -> encryptor((byte) 2).decrypt(encrypted));
    }

    @Test
    void 변조된_암호문은_복호화할_수_없다() {
        String encrypted = encryptor.encrypt("secret-password");
        String base64Payload = encrypted.substring(AesGcmStringEncryptor.ENCRYPTED_PREFIX.length());
        byte[] payload = Base64.getDecoder().decode(base64Payload);
        payload[payload.length - 1] ^= 1;
        String tampered = AesGcmStringEncryptor.ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(payload);

        assertThrows(CryptoException.class, () -> encryptor.decrypt(tampered));
    }

    @Test
    void null과_빈문자열은_그대로_반환한다() {
        assertNull(encryptor.encrypt(null));
        assertNull(encryptor.decrypt(null));
        assertEquals("", encryptor.encrypt(""));
        assertEquals("", encryptor.decrypt(""));
    }

    @Test
    void 기존_평문은_복호화_대상으로_취급하지_않는다() {
        assertEquals("legacy-password", encryptor.decrypt("legacy-password"));
    }

    private StringEncryptor encryptor(byte seed) {
        byte[] keyBytes = new byte[32];
        java.util.Arrays.fill(keyBytes, seed);
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        return new AesGcmStringEncryptor(() -> key);
    }
}
