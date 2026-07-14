package io.dbflow.infrastructure.repository;

import io.dbflow.common.exception.RepositoryException;
import io.dbflow.common.DateTimeHelper;
import io.dbflow.domain.MigrationScript;
import io.dbflow.infrastructure.path.DbFlowFilePermissions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class InstallRepository {

    private static final String SQLITE_URL_PREFIX = "jdbc:sqlite:";
    private static final String DATABASE_CREATION_FAILED = "DBFlow 제품 데이터베이스를 생성하지 못했습니다.";
    private static final String MIGRATION_FAILED = "DBFlow DB 마이그레이션 실행에 실패했습니다.";
    private static final String CHECKSUM_MISMATCH = "이미 적용된 DB 마이그레이션 파일의 체크섬이 일치하지 않습니다.";

    public void createDatabase(Path databasePath) {
        Path normalizedDatabasePath = databasePath.toAbsolutePath().normalize();
        String databaseUrl = SQLITE_URL_PREFIX + normalizedDatabasePath;

        try (Connection connection = DriverManager.getConnection(databaseUrl)) {
            boolean databaseFileCreated = Files.isRegularFile(normalizedDatabasePath, LinkOption.NOFOLLOW_LINKS);
            if (!databaseFileCreated || !connection.isValid(1)) {
                throw new RepositoryException(DATABASE_CREATION_FAILED);
            }
            DbFlowFilePermissions.applyFilePermissions(normalizedDatabasePath);
        } catch (SQLException | IOException e) {
            throw new RepositoryException(DATABASE_CREATION_FAILED, e);
        }
    }

    public void executeMigrations(
            Path databasePath,
            String appVersion,
            List<MigrationScript> scripts
    ) {
        String databaseUrl = SQLITE_URL_PREFIX + databasePath.toAbsolutePath().normalize();
        for (MigrationScript script : scripts) {
            executeMigration(databaseUrl, appVersion, script);
        }
    }

    private void executeMigration(String databaseUrl, String appVersion, MigrationScript script) {
        try (Connection connection = DriverManager.getConnection(databaseUrl)) {
            connection.setAutoCommit(false);
            try {
                String successfulChecksum = findSuccessfulChecksum(connection, script);
                if (successfulChecksum != null) {
                    if (!successfulChecksum.equals(script.checksum())) {
                        throw new RepositoryException(CHECKSUM_MISMATCH + " " + script.scriptName());
                    }
                    connection.rollback();
                    return;
                }

                int currentPatch = findCurrentPatch(connection, script);
                if (script.patchVersion() <= currentPatch) {
                    connection.rollback();
                    return;
                }

                executeSqlScript(connection, script.sql());
                saveSchemaVersion(connection, script);
                saveMigrationHistory(connection, appVersion, script, "SUCCESS", null);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                saveFailedHistory(databaseUrl, appVersion, script, e.getMessage());
                if (e instanceof RepositoryException repositoryException) {
                    throw repositoryException;
                }
                throw new RepositoryException(MIGRATION_FAILED + " " + script.scriptName(), e);
            }
        } catch (SQLException e) {
            throw new RepositoryException(MIGRATION_FAILED + " " + script.scriptName(), e);
        }
    }

    private String findSuccessfulChecksum(Connection connection, MigrationScript script) throws SQLException {
        if (!tableExists(connection, "DBF_SCHEMA_MIGRATION_HISTORY")) {
            return null;
        }
        String sql = """
                SELECT SCRIPT_CHECKSUM
                  FROM DBF_SCHEMA_MIGRATION_HISTORY
                 WHERE MAJOR_VERSION = ? AND MINOR_VERSION = ? AND PATCH_VERSION = ?
                   AND STATUS = 'SUCCESS'
                 ORDER BY MIGRATION_HISTORY_ID DESC
                 LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, script.majorVersion());
            statement.setInt(2, script.minorVersion());
            statement.setInt(3, script.patchVersion());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString(1) : null;
            }
        }
    }

    private int findCurrentPatch(Connection connection, MigrationScript script) throws SQLException {
        if (!tableExists(connection, "DBF_SCHEMA_VERSION")) {
            return -1;
        }
        String sql = "SELECT PATCH_VERSION FROM DBF_SCHEMA_VERSION WHERE MAJOR_VERSION = ? AND MINOR_VERSION = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, script.majorVersion());
            statement.setInt(2, script.minorVersion());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : -1;
            }
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void executeSqlScript(Connection connection, String scriptSql) throws SQLException {
        String sqlWithoutComments = scriptSql
                .replaceAll("(?s)/\\*.*?\\*/", "")
                .replaceAll("(?m)--.*$", "");
        try (Statement statement = connection.createStatement()) {
            for (String sql : sqlWithoutComments.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql.trim());
                }
            }
        }
    }

    private void saveSchemaVersion(Connection connection, MigrationScript script) throws SQLException {
        String sql = """
                INSERT INTO DBF_SCHEMA_VERSION
                    (MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION, CREATED_AT, UPDATED_AT)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(MAJOR_VERSION, MINOR_VERSION) DO UPDATE SET
                    PATCH_VERSION = excluded.PATCH_VERSION,
                    UPDATED_AT = excluded.UPDATED_AT
                """;
        String now = DateTimeHelper.now();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, script.majorVersion());
            statement.setInt(2, script.minorVersion());
            statement.setInt(3, script.patchVersion());
            statement.setString(4, now);
            statement.setString(5, now);
            statement.executeUpdate();
        }
    }

    private void saveMigrationHistory(
            Connection connection,
            String appVersion,
            MigrationScript script,
            String status,
            String errorMessage
    ) throws SQLException {
        String sql = """
                INSERT INTO DBF_SCHEMA_MIGRATION_HISTORY
                    (MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION, APP_VERSION,
                     SCRIPT_NAME, SCRIPT_CHECKSUM, STATUS, CREATED_AT, ERROR_MESSAGE)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, script.majorVersion());
            statement.setInt(2, script.minorVersion());
            statement.setInt(3, script.patchVersion());
            statement.setString(4, appVersion);
            statement.setString(5, script.scriptName());
            statement.setString(6, script.checksum());
            statement.setString(7, status);
            statement.setString(8, DateTimeHelper.now());
            statement.setString(9, errorMessage);
            statement.executeUpdate();
        }
    }

    private void saveFailedHistory(
            String databaseUrl,
            String appVersion,
            MigrationScript script,
            String errorMessage
    ) {
        try (Connection connection = DriverManager.getConnection(databaseUrl)) {
            if (!tableExists(connection, "DBF_SCHEMA_MIGRATION_HISTORY")) {
                return;
            }
            saveMigrationHistory(connection, appVersion, script, "FAILED", errorMessage);
        } catch (SQLException ignored) {
            // 최초 스키마 생성 실패 등 이력 테이블이 없거나 기록할 수 없는 경우 원래 예외를 유지한다.
        }
    }
}
