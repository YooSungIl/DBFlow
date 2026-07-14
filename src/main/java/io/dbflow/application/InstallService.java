package io.dbflow.application;

import io.dbflow.common.exception.ServiceException;
import io.dbflow.infrastructure.repository.InstallRepository;
import io.dbflow.infrastructure.path.DbFlowFilePermissions;
import io.dbflow.infrastructure.path.DbFlowPathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class InstallService {

    private static final String DATA_DIRECTORY_NAME = "data";
    private static final String SECURITY_DIRECTORY_NAME = "security";
    private static final String DATABASE_FILE_NAME = "dbflow.db";

    private final Path userDataDirectory;
    private final InstallRepository installRepository;
    private final DatabaseMigrationService databaseMigrationService;

    public InstallService() {
        this(DbFlowPathResolver.resolveUserDataDirectory());
    }

    public InstallService(Path userDataDirectory) {
        this(userDataDirectory, new InstallRepository());
    }

    public InstallService(Path userDataDirectory, InstallRepository installRepository) {
        this(userDataDirectory, installRepository, new DatabaseMigrationService(installRepository));
    }

    public InstallService(
            Path userDataDirectory,
            InstallRepository installRepository,
            DatabaseMigrationService databaseMigrationService
    ) {
        this.userDataDirectory = userDataDirectory;
        this.installRepository = installRepository;
        this.databaseMigrationService = databaseMigrationService;
    }

    public void install() {
        if (Files.exists(userDataDirectory, LinkOption.NOFOLLOW_LINKS)) {
            throw new ServiceException(ServiceException.INSTALL_DIRECTORY_ALREADY_EXISTS);
        }

        try {
            Files.createDirectory(userDataDirectory);
            DbFlowFilePermissions.applyDirectoryPermissions(userDataDirectory);

            createUserDirectories();
            createDatabase();
            databaseMigrationService.migrate(getDatabasePath());
        } catch (Exception e) {
            rollbackInstall();
            throwInstallException(e);
        }
    }

    private void createUserDirectories() throws IOException {
        Path dataDirectory = userDataDirectory.resolve(DATA_DIRECTORY_NAME);
        Path securityDirectory = userDataDirectory.resolve(SECURITY_DIRECTORY_NAME);

        Files.createDirectory(dataDirectory);
        DbFlowFilePermissions.applyDirectoryPermissions(dataDirectory);
        Files.createDirectory(securityDirectory);
        DbFlowFilePermissions.applyDirectoryPermissions(securityDirectory);
    }

    private void createDatabase() {
        Path databasePath = getDatabasePath();
        if (Files.exists(databasePath, LinkOption.NOFOLLOW_LINKS)) {
            throw new ServiceException(ServiceException.INSTALL_DATABASE_ALREADY_EXISTS);
        }

        installRepository.createDatabase(databasePath);
    }

    private void rollbackInstall() {
        deleteCreatedFile(getDatabasePath());
        deleteCreatedDirectory(userDataDirectory.resolve(SECURITY_DIRECTORY_NAME));
        deleteCreatedDirectory(userDataDirectory.resolve(DATA_DIRECTORY_NAME));
        deleteCreatedDirectory(userDataDirectory);
    }

    private Path getDatabasePath() {
        return userDataDirectory.resolve(DATA_DIRECTORY_NAME).resolve(DATABASE_FILE_NAME);
    }

    private void deleteCreatedFile(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // install()이 생성한 파일만 정리하며, 실패 원인은 최초 설치 예외를 유지한다.
        }
    }

    private void deleteCreatedDirectory(Path directory) {
        try {
            Files.deleteIfExists(directory);
        } catch (IOException ignored) {
            // 설치 전에 존재하지 않았던 빈 디렉터리만 정리한다.
        }
    }

    private void throwInstallException(Exception exception) {
        if (exception instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new ServiceException(ServiceException.INSTALL_DIRECTORY_CREATION_FAILED, exception);
    }
}
