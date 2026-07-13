package io.dbflow.infrastructure.security;

import io.dbflow.common.exception.CryptoException;
import io.dbflow.common.security.EncryptionKeyProvider;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class FileEncryptionKeyProvider implements EncryptionKeyProvider {

    private static final int KEY_SIZE_BITS = 256;
    private static final int KEY_SIZE_BYTES = KEY_SIZE_BITS / Byte.SIZE;
    private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS = Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE
    );
    private static final Set<PosixFilePermission> FILE_PERMISSIONS = Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE
    );

    private final Path keyPath;

    public FileEncryptionKeyProvider(Path keyPath) {
        this.keyPath = keyPath;
    }

    @Override
    public SecretKey getKey() {
        try {
            createKeyIfMissing();
            byte[] keyBytes = Files.readAllBytes(keyPath);
            if (keyBytes.length != KEY_SIZE_BYTES) {
                throw new CryptoException(CryptoException.KEY_LOAD_FAILED);
            }
            applyFilePermissions(keyPath);
            return new SecretKeySpec(keyBytes, "AES");
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException(CryptoException.KEY_LOAD_FAILED, e);
        }
    }

    private void createKeyIfMissing() throws Exception {
        Path parent = keyPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
            applyDirectoryPermissions(parent);
        }

        if (Files.exists(keyPath)) {
            return;
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(KEY_SIZE_BITS);
        byte[] keyBytes = keyGenerator.generateKey().getEncoded();

        try {
            Files.write(keyPath, keyBytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            applyFilePermissions(keyPath);
        } catch (FileAlreadyExistsException ignored) {
            // 다른 프로세스가 먼저 생성한 키를 아래 getKey()에서 읽는다.
        }
    }

    private void applyDirectoryPermissions(Path directory) throws Exception {
        try {
            Files.setPosixFilePermissions(directory, DIRECTORY_PERMISSIONS);
        } catch (UnsupportedOperationException ignored) {
            // POSIX 권한을 지원하지 않는 파일 시스템에서는 기본 권한을 사용한다.
        }
    }

    private void applyFilePermissions(Path file) throws Exception {
        try {
            Files.setPosixFilePermissions(file, FILE_PERMISSIONS);
        } catch (UnsupportedOperationException ignored) {
            // POSIX 권한을 지원하지 않는 파일 시스템에서는 기본 권한을 사용한다.
        }
    }
}
