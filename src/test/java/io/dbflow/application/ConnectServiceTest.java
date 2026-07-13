package io.dbflow.application;

import io.dbflow.common.exception.ValidationException;
import io.dbflow.common.security.AesGcmStringEncryptor;
import io.dbflow.common.security.StringEncryptor;
import io.dbflow.domain.DbConfig;
import io.dbflow.infrastructure.dbms.DbConnection;
import io.dbflow.infrastructure.dbms.DbConnectionFactory;
import io.dbflow.infrastructure.repository.DbConfigRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectServiceTest {

    @Test
    void 사용가능한_새_DB별칭을_허용한다() {
        ConnectService service = new ConnectService(new StubDbConfigRepository(null));

        assertDoesNotThrow(() -> service.validateNewAlias("local-db_01"));
    }

    @Test
    void 잘못된_형식의_DB별칭을_허용하지_않는다() {
        ConnectService service = new ConnectService(new StubDbConfigRepository(null));

        assertThrows(ValidationException.class, () -> service.validateNewAlias("local db"));
    }

    @Test
    void 이미_등록된_DB별칭을_허용하지_않는다() {
        ConnectService service = new ConnectService(new StubDbConfigRepository(new DbConfig("local")));

        ValidationException exception = assertThrows(ValidationException.class, () -> service.validateNewAlias("local"));

        assertEquals(ValidationException.DUPLICATED_DB_ALIAS, exception.getMessage());
    }

    @Test
    void 저장_전에는_평문으로_연결을_테스트하고_Repository에는_암호문을_전달한다() {
        StringEncryptor encryptor = encryptor();
        StubDbConfigRepository repository = new StubDbConfigRepository(null);
        CapturingDbConnection connection = new CapturingDbConnection(encryptor);
        ConnectService service = new ConnectService(repository, new StubDbConnectionFactory(connection), encryptor);
        DbConfig dbConfig = dbConfig("plain-password");

        service.saveDbConnect(dbConfig);

        assertEquals("plain-password", connection.receivedPassword);
        assertTrue(encryptor.isEncrypted(repository.savedPassword));
        assertEquals("plain-password", encryptor.decrypt(repository.savedPassword));
    }

    @Test
    void 저장된_암호문은_연결_직전에만_복호화하고_DbConfig에는_다시_저장하지_않는다() {
        StringEncryptor encryptor = encryptor();
        String encryptedPassword = encryptor.encrypt("plain-password");
        StubDbConfigRepository repository = new StubDbConfigRepository(null);
        CapturingDbConnection connection = new CapturingDbConnection(encryptor);
        ConnectService service = new ConnectService(repository, new StubDbConnectionFactory(connection), encryptor);
        DbConfig dbConfig = dbConfig(encryptedPassword);

        service.updateDbConfig(dbConfig);

        assertEquals("plain-password", connection.receivedPassword);
        assertEquals(encryptedPassword, dbConfig.getDbPassword());
        assertEquals(encryptedPassword, repository.updatedPassword);
    }

    private StringEncryptor encryptor() {
        byte[] keyBytes = new byte[32];
        java.util.Arrays.fill(keyBytes, (byte) 7);
        return new AesGcmStringEncryptor(() -> new javax.crypto.spec.SecretKeySpec(keyBytes, "AES"));
    }

    private DbConfig dbConfig(String password) {
        return new DbConfig("local", "POSTGRESQL", "localhost", 5432, "testdb", "public", "tester", password);
    }

    private static class StubDbConfigRepository extends DbConfigRepository {
        private final DbConfig dbConfig;
        private String savedPassword;
        private String updatedPassword;

        private StubDbConfigRepository(DbConfig dbConfig) {
            this.dbConfig = dbConfig;
        }

        @Override
        public DbConfig findDbConfig(String dbAlias) {
            return dbConfig;
        }

        @Override
        public void saveDbConfig(DbConfig dbConfig) {
            savedPassword = dbConfig.getDbPassword();
        }

        @Override
        public void updateDbConfig(DbConfig dbConfig) {
            updatedPassword = dbConfig.getDbPassword();
        }
    }

    private static class StubDbConnectionFactory extends DbConnectionFactory {
        private final DbConnection connection;

        private StubDbConnectionFactory(DbConnection connection) {
            this.connection = connection;
        }

        @Override
        public DbConnection create(String dbType) {
            return connection;
        }
    }

    private static class CapturingDbConnection implements DbConnection {
        private final StringEncryptor encryptor;
        private String receivedPassword;

        private CapturingDbConnection(StringEncryptor encryptor) {
            this.encryptor = encryptor;
        }

        @Override
        public void testConnection(DbConfig config) {
            receivedPassword = encryptor.decrypt(config.getDbPassword());
        }
    }
}
