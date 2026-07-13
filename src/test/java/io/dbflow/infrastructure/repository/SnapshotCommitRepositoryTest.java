package io.dbflow.infrastructure.repository;

import io.dbflow.application.MetadataCollectService;
import io.dbflow.application.CommitService;
import io.dbflow.application.UserService;
import io.dbflow.application.WorkService;
import io.dbflow.common.exception.ServiceException;
import io.dbflow.domain.WorkChange;
import io.dbflow.domain.WorkComponent;
import io.dbflow.domain.WorkDiffResult;
import io.dbflow.domain.WorkTarget;
import io.dbflow.domain.TableSnapshot;
import io.dbflow.domain.ColumnMetadata;
import io.dbflow.domain.CommitLog;
import io.dbflow.domain.DbConfig;
import io.dbflow.domain.TableMetadata;
import io.dbflow.infrastructure.external.repository.MetadataCollector;
import io.dbflow.infrastructure.mybatis.MainMyBatisSqlSessionFactory;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SnapshotCommitRepositoryTest {

    private final SnapshotRepository snapshotRepository = new SnapshotRepository();

    @BeforeAll
    static void prepareTestDbFile() throws Exception {
        Path dbDir = Path.of("build", "dbflow-test");
        Files.createDirectories(dbDir);
        Files.deleteIfExists(dbDir.resolve("dbflow.db"));
    }

    @BeforeEach
    void resetSchema() throws Exception {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            Connection connection = session.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("PRAGMA foreign_keys = OFF");
            dropTables(statement);
            String schemaSql = Files.readString(Path.of("docs", "sql", "init_schema.sql"))
                    .replaceAll("(?s)/\\*.*?\\*/", "");
            for (String sql : schemaSql.split(";")) {
                String statementSql = sql.trim();
                if (!statementSql.isBlank()) {
                    statement.execute(statementSql);
                }
            }
            statement.close();
            session.commit();
        }
    }

    @Test
    void collectRollbackKeepsPreviousSnapshotWhenColumnCollectFails() throws Exception {
        Long dbConfigId = insertDbConfig("db1");
        Long otherDbConfigId = insertDbConfig("db2");
        insertCollectTable(dbConfigId, "old_table");
        insertCollectTable(otherDbConfigId, "other_table");

        DbConfig dbConfig = dbConfig(dbConfigId, "db1");
        MetadataCollectService service = new MetadataCollectService(new FailingColumnCollector());

        assertThrows(IllegalStateException.class, () -> service.collect(dbConfig));

        assertEquals(1, count("DBF_COLLECT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId + " AND TABLE_NAME = 'old_table'"));
        assertEquals(0, count("DBF_COLLECT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId + " AND TABLE_NAME = 'new_table'"));
        assertEquals(1, count("DBF_COLLECT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + otherDbConfigId));
    }

    @Test
    void deleteCollectedSnapshotDeletesOnlyRequestedDbConfig() throws Exception {
        Long dbConfigId = insertDbConfig("db1");
        Long otherDbConfigId = insertDbConfig("db2");
        Long tableId = insertCollectTable(dbConfigId, "db1_table");
        Long otherTableId = insertCollectTable(otherDbConfigId, "db2_table");
        insertCollectColumn(tableId, "db1_column");
        insertCollectColumn(otherTableId, "db2_column");

        snapshotRepository.deleteCollectedSnapshot(dbConfigId);

        assertEquals(0, count("DBF_COLLECT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId));
        assertEquals(0, count("DBF_COLLECT_COLUMN_SNAPSHOT", "COLLECT_TABLE_ID = " + tableId));
        assertEquals(1, count("DBF_COLLECT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + otherDbConfigId));
        assertEquals(1, count("DBF_COLLECT_COLUMN_SNAPSHOT", "COLLECT_TABLE_ID = " + otherTableId));
    }

    @Test
    void commitWorkCopiesOnlyRequestedDbConfigWorkRows() throws Exception {
        Long dbConfigId = insertDbConfig("db1");
        Long otherDbConfigId = insertDbConfig("db2");
        Long userId = insertUser(dbConfigId);
        insertWorkTree(dbConfigId, "db1_table", "db1_column");
        insertWorkTree(otherDbConfigId, "db2_table", "db2_column");

        CommitLog commitLog = commitLog(dbConfigId, userId);
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            new CommitRepository().commitWork(commitLog, session);
            session.commit();
        }

        assertEquals(1, count("DBF_COMMIT_TARGET", "COMMIT_LOG_ID = " + commitLog.getCommitLogId()));
        assertEquals(1, count("DBF_COMMIT_COMPONENT", "1 = 1"));
        assertEquals(1, count("DBF_COMMIT_CHANGE", "1 = 1"));
        assertEquals(0, count("DBF_COMMIT_TARGET", "DB_CONFIG_ID = " + otherDbConfigId));
    }

    @Test
    void currentAndHistorySnapshotsCopyOnlyRequestedDbConfigCollectRows() throws Exception {
        Long dbConfigId = insertDbConfig("db1");
        Long otherDbConfigId = insertDbConfig("db2");
        Long userId = insertUser(dbConfigId);
        Long tableId = insertCollectTable(dbConfigId, "db1_table");
        Long otherTableId = insertCollectTable(otherDbConfigId, "db2_table");
        insertCollectColumn(tableId, "db1_column");
        insertCollectColumn(otherTableId, "db2_column");

        CommitLog commitLog = commitLog(dbConfigId, userId);
        commitLog.setCommitLogId(insertCommitLog(commitLog));
        insertCommitTarget(commitLog.getCommitLogId(), dbConfigId, "db1_table", "MOD");

        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            snapshotRepository.insertCommitCurrentSnapshot(commitLog, session);
            snapshotRepository.insertCommitHistorySnapshot(commitLog, session);
            session.commit();
        }

        assertEquals(1, count("DBF_CURRENT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId));
        assertEquals(0, count("DBF_CURRENT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + otherDbConfigId));
        assertEquals(1, count("DBF_CURRENT_COLUMN_SNAPSHOT", "1 = 1"));
        assertEquals(1, count("DBF_HISTORY_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId));
        assertEquals(0, count("DBF_HISTORY_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + otherDbConfigId));
        assertEquals(1, count("DBF_HISTORY_COLUMN_SNAPSHOT", "1 = 1"));
    }

    @Test
    void currentSnapshotKeepsUnchangedTablesWhenOnlyBIsModified() throws Exception {
        Long dbConfigId = insertDbConfig("db1");
        Long userId = insertUser(dbConfigId);
        Long previousCommitLogId = insertCommitLog(commitLog(dbConfigId, userId));
        insertCurrentColumn(insertCurrentTable(previousCommitLogId, dbConfigId, "A"), "A_column");
        insertCurrentColumn(insertCurrentTable(previousCommitLogId, dbConfigId, "B"), "B_old_column");
        insertCurrentColumn(insertCurrentTable(previousCommitLogId, dbConfigId, "C"), "C_column");

        Long collectTableId = insertCollectTable(dbConfigId, "B");
        insertCollectColumn(collectTableId, "B_new_column");

        CommitLog commitLog = commitLog(dbConfigId, userId);
        commitLog.setCommitLogId(insertCommitLog(commitLog));
        insertCommitTarget(commitLog.getCommitLogId(), dbConfigId, "B", "MOD");

        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            snapshotRepository.insertCommitCurrentSnapshot(commitLog, session);
            session.commit();
        }

        assertEquals(3, count("DBF_CURRENT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId));
        assertEquals(1, count("DBF_CURRENT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId + " AND TABLE_NAME = 'A'"));
        assertEquals(1, count("DBF_CURRENT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId + " AND TABLE_NAME = 'B'"));
        assertEquals(1, count("DBF_CURRENT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId + " AND TABLE_NAME = 'C'"));
        assertEquals(1, count("DBF_CURRENT_COLUMN_SNAPSHOT", "COLUMN_NAME = 'B_new_column'"));
        assertEquals(0, count("DBF_CURRENT_COLUMN_SNAPSHOT", "COLUMN_NAME = 'B_old_column'"));
    }

    @Test
    void currentSnapshotRemovesOnlyBWhenBIsDeleted() throws Exception {
        Long dbConfigId = insertDbConfig("db1");
        Long userId = insertUser(dbConfigId);
        Long previousCommitLogId = insertCommitLog(commitLog(dbConfigId, userId));
        insertCurrentColumn(insertCurrentTable(previousCommitLogId, dbConfigId, "A"), "A_column");
        insertCurrentColumn(insertCurrentTable(previousCommitLogId, dbConfigId, "B"), "B_column");
        insertCurrentColumn(insertCurrentTable(previousCommitLogId, dbConfigId, "C"), "C_column");

        CommitLog commitLog = commitLog(dbConfigId, userId);
        commitLog.setCommitLogId(insertCommitLog(commitLog));
        insertCommitTarget(commitLog.getCommitLogId(), dbConfigId, "B", "DEL");

        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            snapshotRepository.insertCommitCurrentSnapshot(commitLog, session);
            session.commit();
        }

        assertEquals(2, count("DBF_CURRENT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId));
        assertEquals(1, count("DBF_CURRENT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId + " AND TABLE_NAME = 'A'"));
        assertEquals(0, count("DBF_CURRENT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId + " AND TABLE_NAME = 'B'"));
        assertEquals(1, count("DBF_CURRENT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId + " AND TABLE_NAME = 'C'"));
        assertEquals(0, count("DBF_CURRENT_COLUMN_SNAPSHOT", "COLUMN_NAME = 'B_column'"));
    }

    @Test
    void commitServiceCommitsWorkAndSnapshotsInOneTransaction() throws Exception {
        Long dbConfigId = insertDbConfig("db1");
        insertUser(dbConfigId);
        insertWorkTree(dbConfigId, "member", "member_id");
        Long collectTableId = insertCollectTable(dbConfigId, "member");
        insertCollectColumn(collectTableId, "member_id");

        CommitService commitService = new CommitService(
                new CommitRepository(),
                new SnapshotRepository(),
                new UserService(new UserRepository()),
                new WorkService()
        );

        commitService.commit("통합 테스트", "Commit 전체 흐름");

        assertEquals(1, count("DBF_COMMIT_LOG", "DB_CONFIG_ID = " + dbConfigId));
        assertEquals(1, count("DBF_COMMIT_TARGET", "DB_CONFIG_ID = " + dbConfigId));
        assertEquals(1, count("DBF_HISTORY_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId));
        assertEquals(1, count("DBF_HISTORY_COLUMN_SNAPSHOT", "1 = 1"));
        assertEquals(1, count("DBF_CURRENT_TABLE_SNAPSHOT", "DB_CONFIG_ID = " + dbConfigId));
        assertEquals(1, count("DBF_CURRENT_COLUMN_SNAPSHOT", "1 = 1"));
    }

    @Test
    void commitServiceRollsBackEverythingWhenSnapshotSaveFails() throws Exception {
        Long dbConfigId = insertDbConfig("db1");
        insertUser(dbConfigId);
        insertWorkTree(dbConfigId, "member", "member_id");

        CommitService commitService = new CommitService(
                new CommitRepository(),
                new FailingSnapshotRepository(),
                new UserService(new UserRepository()),
                new WorkService()
        );

        assertThrows(ServiceException.class, () -> commitService.commit("실패 테스트", "Rollback 확인"));

        assertEquals(0, count("DBF_COMMIT_LOG", "DB_CONFIG_ID = " + dbConfigId));
        assertEquals(0, count("DBF_COMMIT_TARGET", "DB_CONFIG_ID = " + dbConfigId));
        assertEquals(0, count("DBF_COMMIT_COMPONENT", "1 = 1"));
        assertEquals(0, count("DBF_COMMIT_CHANGE", "1 = 1"));
        assertEquals(1, count("DBF_WORK_TARGET", "DB_CONFIG_ID = " + dbConfigId));
    }

    @Test
    void workRepositoryReplacesAndAssemblesOnlyRequestedDbConfigDiff() throws Exception {
        Long dbConfigId = insertDbConfig("db1");
        Long otherDbConfigId = insertDbConfig("db2");
        insertWorkTree(otherDbConfigId, "other_table", "other_column");

        WorkDiffResult result = new WorkDiffResult();
        WorkTarget target = result.addTarget(new WorkTarget(null, "TABLE", "member", "회원", "MOD"));
        WorkComponent component = target.addComponent(new WorkComponent("COLUMN", "member_id", "회원 ID", "MOD"));
        component.addChange(new WorkChange("DATA_TYPE", "integer", "bigint"));

        WorkRepository workRepository = new WorkRepository();
        workRepository.replace(dbConfigId, result);
        List<WorkTarget> storedTargets = workRepository.findWorkDiff(dbConfigId);

        assertEquals(1, storedTargets.size());
        assertEquals(dbConfigId, storedTargets.get(0).getDbConfigId());
        assertEquals("member", storedTargets.get(0).getObjectName());
        assertEquals(1, storedTargets.get(0).getComponents().size());
        assertEquals("member_id", storedTargets.get(0).getComponents().get(0).getComponentName());
        assertEquals(1, storedTargets.get(0).getComponents().get(0).getChanges().size());
        assertEquals("DATA_TYPE", storedTargets.get(0).getComponents().get(0).getChanges().get(0).getChangeColumn());
        assertEquals(1, count("DBF_WORK_TARGET", "DB_CONFIG_ID = " + otherDbConfigId));
    }

    private static void dropTables(Statement statement) throws Exception {
        List<String> tables = List.of(
                "DBF_HISTORY_INDEX_SNAPSHOT",
                "DBF_HISTORY_COLUMN_SNAPSHOT",
                "DBF_HISTORY_TABLE_SNAPSHOT",
                "DBF_CURRENT_INDEX_SNAPSHOT",
                "DBF_CURRENT_COLUMN_SNAPSHOT",
                "DBF_CURRENT_TABLE_SNAPSHOT",
                "DBF_COLLECT_INDEX_SNAPSHOT",
                "DBF_COLLECT_COLUMN_SNAPSHOT",
                "DBF_COLLECT_TABLE_SNAPSHOT",
                "DBF_COMMIT_CHANGE",
                "DBF_COMMIT_COMPONENT",
                "DBF_COMMIT_TARGET",
                "DBF_COMMIT_LOG",
                "DBF_WORK_CHANGE",
                "DBF_WORK_COMPONENT",
                "DBF_WORK_TARGET",
                "DBF_USER",
                "DBF_DB_CONFIG"
        );

        for (String table : tables) {
            statement.execute("DROP TABLE IF EXISTS " + table);
        }
    }

    private Long insertDbConfig(String alias) throws Exception {
        String sql = """
                INSERT INTO DBF_DB_CONFIG
                (DB_ALIAS, DB_TYPE, DB_HOST, DB_PORT, DB_NAME, DB_SCHEMA, DB_USER, DB_PASSWORD, USE_YN, CREATED_AT, UPDATED_AT)
                VALUES (?, 'POSTGRESQL', 'localhost', ?, ?, 'public', 'user', 'password', 1, ?, ?)
                """;
        return insert(sql, statement -> {
            statement.setString(1, alias);
            statement.setInt(2, 5400 + Math.abs(alias.hashCode() % 1000));
            statement.setString(3, alias + "_db");
            statement.setString(4, Instant.now().toString());
            statement.setString(5, Instant.now().toString());
        });
    }

    private Long insertUser(Long dbConfigId) throws Exception {
        String sql = """
                INSERT INTO DBF_USER (DB_CONFIG_ID, USER_NAME, USER_EMAIL, USE_YN, CREATED_AT, UPDATED_AT)
                VALUES (?, 'tester', 'tester@example.com', 1, ?, ?)
                """;
        return insert(sql, statement -> {
            statement.setLong(1, dbConfigId);
            statement.setString(2, Instant.now().toString());
            statement.setString(3, Instant.now().toString());
        });
    }

    private Long insertCollectTable(Long dbConfigId, String tableName) throws Exception {
        String sql = """
                INSERT INTO DBF_COLLECT_TABLE_SNAPSHOT
                (DB_CONFIG_ID, TABLE_NAME, TABLE_COMMENT, TABLE_TYPE, OWNER_NAME)
                VALUES (?, ?, ?, 'TABLE', 'owner')
                """;
        return insert(sql, statement -> {
            statement.setLong(1, dbConfigId);
            statement.setString(2, tableName);
            statement.setString(3, tableName + " comment");
        });
    }

    private Long insertCollectColumn(Long collectTableId, String columnName) throws Exception {
        String sql = """
                INSERT INTO DBF_COLLECT_COLUMN_SNAPSHOT
                (COLLECT_TABLE_ID, COLUMN_NAME, COLUMN_COMMENT, COLUMN_ORDER, DATA_TYPE, DATA_LENGTH, DATA_SCALE, NULLABLE_YN, DEFAULT_VALUE, IDENTITY_YN, IDENTITY_TYPE)
                VALUES (?, ?, ?, 1, 'varchar', 100, NULL, 1, NULL, 0, NULL)
                """;
        return insert(sql, statement -> {
            statement.setLong(1, collectTableId);
            statement.setString(2, columnName);
            statement.setString(3, columnName + " comment");
        });
    }

    private Long insertCurrentTable(Long commitLogId, Long dbConfigId, String tableName) throws Exception {
        String sql = """
                INSERT INTO DBF_CURRENT_TABLE_SNAPSHOT
                (COMMIT_LOG_ID, DB_CONFIG_ID, TABLE_NAME, TABLE_COMMENT, TABLE_TYPE, OWNER_NAME)
                VALUES (?, ?, ?, ?, 'TABLE', 'owner')
                """;
        return insert(sql, statement -> {
            statement.setLong(1, commitLogId);
            statement.setLong(2, dbConfigId);
            statement.setString(3, tableName);
            statement.setString(4, tableName + " comment");
        });
    }

    private Long insertCurrentColumn(Long currentTableId, String columnName) throws Exception {
        String sql = """
                INSERT INTO DBF_CURRENT_COLUMN_SNAPSHOT
                (CURRENT_TABLE_ID, COLUMN_NAME, COLUMN_COMMENT, COLUMN_ORDER, DATA_TYPE, DATA_LENGTH, DATA_SCALE, NULLABLE_YN, DEFAULT_VALUE, IDENTITY_YN, IDENTITY_TYPE)
                VALUES (?, ?, ?, 1, 'varchar', 100, NULL, 1, NULL, 0, NULL)
                """;
        return insert(sql, statement -> {
            statement.setLong(1, currentTableId);
            statement.setString(2, columnName);
            statement.setString(3, columnName + " comment");
        });
    }

    private Long insertCommitTarget(Long commitLogId, Long dbConfigId, String tableName, String changeType) throws Exception {
        String sql = """
                INSERT INTO DBF_COMMIT_TARGET
                (COMMIT_LOG_ID, DB_CONFIG_ID, OBJECT_TYPE, OBJECT_NAME, OBJECT_COMMENT, CHANGE_TYPE)
                VALUES (?, ?, 'TABLE', ?, ?, ?)
                """;
        return insert(sql, statement -> {
            statement.setLong(1, commitLogId);
            statement.setLong(2, dbConfigId);
            statement.setString(3, tableName);
            statement.setString(4, tableName + " comment");
            statement.setString(5, changeType);
        });
    }

    private void insertWorkTree(Long dbConfigId, String tableName, String columnName) throws Exception {
        Long targetId = insert("""
                INSERT INTO DBF_WORK_TARGET
                (DB_CONFIG_ID, OBJECT_TYPE, OBJECT_NAME, OBJECT_COMMENT, CHANGE_TYPE, COMPARED_AT)
                VALUES (?, 'TABLE', ?, ?, 'MOD', datetime('now', 'localtime'))
                """, statement -> {
            statement.setLong(1, dbConfigId);
            statement.setString(2, tableName);
            statement.setString(3, tableName + " comment");
        });

        Long componentId = insert("""
                INSERT INTO DBF_WORK_COMPONENT
                (WORK_TARGET_ID, COMPONENT_TYPE, COMPONENT_NAME, COMPONENT_COMMENT, CHANGE_TYPE)
                VALUES (?, 'COLUMN', ?, ?, 'MOD')
                """, statement -> {
            statement.setLong(1, targetId);
            statement.setString(2, columnName);
            statement.setString(3, columnName + " comment");
        });

        insert("""
                INSERT INTO DBF_WORK_CHANGE
                (WORK_COMPONENT_ID, CHANGE_COLUMN, BEFORE_VALUE, AFTER_VALUE)
                VALUES (?, 'DATA_TYPE', 'text', 'varchar')
                """, statement -> statement.setLong(1, componentId));
    }

    private Long insertCommitLog(CommitLog commitLog) throws Exception {
        String sql = """
                INSERT INTO DBF_COMMIT_LOG
                (DB_CONFIG_ID, COMMIT_TITLE, COMMIT_CONTENT, USER_ID, COMMIT_CREATED_AT)
                VALUES (?, ?, ?, ?, ?)
                """;
        return insert(sql, statement -> {
            statement.setLong(1, commitLog.getDbConfigId());
            statement.setString(2, commitLog.getCommitTitle());
            statement.setString(3, commitLog.getCommitContent());
            statement.setLong(4, commitLog.getUserId());
            statement.setString(5, commitLog.getCommitCreatedAt());
        });
    }

    private Long insert(String sql, StatementBinder binder) throws Exception {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            Connection connection = session.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                binder.bind(statement);
                statement.executeUpdate();
                session.commit();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getLong(1);
                    }
                }
            }
        }
        throw new IllegalStateException("Generated key was not returned.");
    }

    private int count(String table, String whereClause) throws Exception {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + whereClause;
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            Connection connection = session.getConnection();
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    private DbConfig dbConfig(Long dbConfigId, String alias) {
        DbConfig dbConfig = new DbConfig(alias, "POSTGRESQL", "localhost", 5432, alias + "_db", "public", "user", "password");
        dbConfig.setDbConfigId(dbConfigId);
        return dbConfig;
    }

    private CommitLog commitLog(Long dbConfigId, Long userId) {
        CommitLog commitLog = new CommitLog();
        commitLog.setDbConfigId(dbConfigId);
        commitLog.setCommitTitle("test commit");
        commitLog.setCommitContent("test content");
        commitLog.setUserId(userId);
        commitLog.setCommitCreatedAt(Timestamp.from(Instant.now()).toString());
        return commitLog;
    }

    private interface StatementBinder {
        void bind(PreparedStatement statement) throws Exception;
    }

    private static class FailingColumnCollector implements MetadataCollector {
        @Override
        public List<TableMetadata> collectTableSnapshotList(DbConfig dbConfig) {
            TableMetadata tableMetadata = new TableMetadata();
            tableMetadata.setTableName("new_table");
            tableMetadata.setTableComment("new table comment");
            tableMetadata.setTableType("TABLE");
            tableMetadata.setTableOwner("owner");
            return List.of(tableMetadata);
        }

        @Override
        public List<ColumnMetadata> collectColumnSnapshotList(DbConfig dbConfig, List<TableSnapshot> tableSnapshot) {
            throw new IllegalStateException("column collect failed");
        }
    }

    private static class FailingSnapshotRepository extends SnapshotRepository {
        @Override
        public void insertCommitHistorySnapshot(CommitLog commitLog, SqlSession session) {
            throw new IllegalStateException("history snapshot save failed");
        }
    }
}
