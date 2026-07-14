package io.dbflow.infrastructure.path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public final class DbFlowFilePermissions {

    private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS = Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE
    );
    private static final Set<PosixFilePermission> FILE_PERMISSIONS = Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE
    );

    private DbFlowFilePermissions() {
    }

    public static void applyDirectoryPermissions(Path directory) throws IOException {
        apply(directory, DIRECTORY_PERMISSIONS);
    }

    public static void applyFilePermissions(Path file) throws IOException {
        apply(file, FILE_PERMISSIONS);
    }

    private static void apply(Path path, Set<PosixFilePermission> permissions) throws IOException {
        try {
            Files.setPosixFilePermissions(path, permissions);
        } catch (UnsupportedOperationException ignored) {
            // POSIX 권한을 지원하지 않는 파일 시스템에서는 기본 권한을 사용한다.
        }
    }
}
