package io.dbflow.infrastructure.dbms;

import io.dbflow.common.security.AesGcmStringEncryptor;
import io.dbflow.common.security.StringEncryptor;
import io.dbflow.domain.DbConfig;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostgresDbConnectionTest {

    @Test
    void 저장된_암호문은_연결용_비밀번호로만_복호화하고_DbConfig는_변경하지_않는다() {
        byte[] keyBytes = new byte[32];
        java.util.Arrays.fill(keyBytes, (byte) 9);
        StringEncryptor encryptor = new AesGcmStringEncryptor(() -> new SecretKeySpec(keyBytes, "AES"));
        String encryptedPassword = encryptor.encrypt("plain-password");
        DbConfig dbConfig = new DbConfig();
        dbConfig.setDbPassword(encryptedPassword);
        PostgresDbConnection connection = new PostgresDbConnection(encryptor);

        String connectionPassword = connection.resolvePasswordForConnection(dbConfig);

        assertEquals("plain-password", connectionPassword);
        assertEquals(encryptedPassword, dbConfig.getDbPassword());
    }
}
