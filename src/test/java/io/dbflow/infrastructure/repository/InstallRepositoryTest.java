package io.dbflow.infrastructure.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InstallRepositoryTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void SQLite_제품_데이터베이스를_생성한다() throws Exception {
        Path databasePath = temporaryDirectory.resolve("dbflow.db");
        InstallRepository installRepository = new InstallRepository();

        installRepository.createDatabase(databasePath);

        assertTrue(Files.isRegularFile(databasePath));
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath)) {
            assertTrue(connection.isValid(1));
        }
    }
}
