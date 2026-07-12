package io.dbflow.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileEncryptionKeyProviderTest {

    @TempDir
    Path tempDirectory;

    @Test
    void 키파일이_없으면_256비트_AES키를_생성한다() throws Exception {
        Path keyPath = tempDirectory.resolve(".dbflow/security/master.key");
        FileEncryptionKeyProvider provider = new FileEncryptionKeyProvider(keyPath);

        SecretKey key = provider.getKey();

        assertTrue(Files.exists(keyPath));
        assertEquals("AES", key.getAlgorithm());
        assertEquals(32, key.getEncoded().length);
        assertEquals(32, Files.size(keyPath));
    }

    @Test
    void 생성된_키를_다시_읽으면_동일한_키를_반환한다() {
        Path keyPath = tempDirectory.resolve(".dbflow/security/master.key");

        SecretKey first = new FileEncryptionKeyProvider(keyPath).getKey();
        SecretKey second = new FileEncryptionKeyProvider(keyPath).getKey();

        assertArrayEquals(first.getEncoded(), second.getEncoded());
    }
}
