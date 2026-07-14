package io.dbflow.application;

import io.dbflow.common.exception.ServiceException;
import io.dbflow.common.DbFlowVersion;
import io.dbflow.infrastructure.repository.InstallRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.nio.file.attribute.PosixFilePermissions;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class InstallServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void 사용자_데이터와_하위_디렉터리를_생성한다() {
        Path userDataDirectory = temporaryDirectory.resolve(".dbflow");
        InstallService installService = new InstallService(userDataDirectory);

        installService.install();

        assertAll(
                () -> assertTrue(Files.isDirectory(userDataDirectory)),
                () -> assertTrue(Files.isDirectory(userDataDirectory.resolve("data"))),
                () -> assertTrue(Files.isDirectory(userDataDirectory.resolve("security"))),
                () -> assertTrue(Files.isRegularFile(userDataDirectory.resolve("data/dbflow.db")))
        );
    }

    @Test
    void 최초_마이그레이션과_버전_및_성공_이력을_저장한다() throws Exception {
        Path userDataDirectory = temporaryDirectory.resolve(".dbflow");
        Path databasePath = userDataDirectory.resolve("data/dbflow.db");
        InstallService installService = new InstallService(userDataDirectory);

        installService.install();

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
             Statement statement = connection.createStatement()) {
            try (ResultSet version = statement.executeQuery("""
                    SELECT MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION
                      FROM DBF_SCHEMA_VERSION
                    """)) {
                assertTrue(version.next());
                assertEquals(1, version.getInt("MAJOR_VERSION"));
                assertEquals(0, version.getInt("MINOR_VERSION"));
                assertEquals(0, version.getInt("PATCH_VERSION"));
            }

            try (ResultSet history = statement.executeQuery("""
                    SELECT APP_VERSION, SCRIPT_NAME, SCRIPT_CHECKSUM, STATUS
                      FROM DBF_SCHEMA_MIGRATION_HISTORY
                    """)) {
                assertTrue(history.next());
                assertEquals(DbFlowVersion.getAppVersion(), history.getString("APP_VERSION"));
                assertEquals("V1.0.0__initial_schema.sql", history.getString("SCRIPT_NAME"));
                assertEquals(64, history.getString("SCRIPT_CHECKSUM").length());
                assertEquals("SUCCESS", history.getString("STATUS"));
            }

            try (ResultSet table = statement.executeQuery("""
                    SELECT 1 FROM sqlite_master
                     WHERE type = 'table' AND name = 'DBF_DB_CONFIG'
                    """)) {
                assertTrue(table.next());
            }
        }
    }

    @Test
    void 사용자_디렉터리와_제품_DB에_소유자_권한만_부여한다() throws Exception {
        assumeTrue(temporaryDirectory.getFileSystem().supportedFileAttributeViews().contains("posix"));
        Path userDataDirectory = temporaryDirectory.resolve(".dbflow");
        InstallService installService = new InstallService(userDataDirectory);

        installService.install();

        assertAll(
                () -> assertEquals(
                        PosixFilePermissions.fromString("rwx------"),
                        Files.getPosixFilePermissions(userDataDirectory)
                ),
                () -> assertEquals(
                        PosixFilePermissions.fromString("rwx------"),
                        Files.getPosixFilePermissions(userDataDirectory.resolve("data"))
                ),
                () -> assertEquals(
                        PosixFilePermissions.fromString("rwx------"),
                        Files.getPosixFilePermissions(userDataDirectory.resolve("security"))
                ),
                () -> assertEquals(
                        PosixFilePermissions.fromString("rw-------"),
                        Files.getPosixFilePermissions(userDataDirectory.resolve("data/dbflow.db"))
                )
        );
    }

    @Test
    void 사용자_데이터_디렉터리가_이미_존재하면_설치를_중단한다() throws Exception {
        Path userDataDirectory = temporaryDirectory.resolve(".dbflow");
        Files.createDirectory(userDataDirectory);
        InstallService installService = new InstallService(userDataDirectory);

        ServiceException exception = assertThrows(ServiceException.class, installService::install);

        assertEquals(ServiceException.INSTALL_DIRECTORY_ALREADY_EXISTS, exception.getMessage());
    }

    @Test
    void 데이터베이스_생성에_실패하면_설치_디렉터리를_모두_원복한다() {
        Path userDataDirectory = temporaryDirectory.resolve(".dbflow");
        InstallRepository failingRepository = new InstallRepository() {
            @Override
            public void createDatabase(Path databasePath) {
                throw new IllegalStateException("database creation failed");
            }
        };
        InstallService installService = new InstallService(userDataDirectory, failingRepository);

        assertThrows(IllegalStateException.class, installService::install);

        assertTrue(Files.notExists(userDataDirectory));
    }
}
