package io.dbflow.application;

import io.dbflow.common.Exception.ValidationException;
import io.dbflow.domain.DbConfig;
import io.dbflow.infrastructure.repository.DbConfigRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    private static class StubDbConfigRepository extends DbConfigRepository {
        private final DbConfig dbConfig;

        private StubDbConfigRepository(DbConfig dbConfig) {
            this.dbConfig = dbConfig;
        }

        @Override
        public DbConfig findDbConfig(String dbAlias) {
            return dbConfig;
        }
    }
}
