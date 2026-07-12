package io.dbflow.command.connect;

import io.dbflow.application.ConnectService;
import io.dbflow.application.UserService;
import io.dbflow.common.console.PromptHelper;
import io.dbflow.domain.DbConfig;
import io.dbflow.infrastructure.repository.DbConfigRepository;
import io.dbflow.infrastructure.repository.UserRepository;
import io.dbflow.testsupport.ConsoleOutputCapture;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectCommandsTest {

    @Test
    void Connect_Add는_입력받은_DB정보를_Service에_전달한다() {
        StubConnectService connectService = new StubConnectService();
        PromptHelper prompt = prompt("local\n1\nlocalhost\n5432\ntestdb\npublic\ntester\npassword\n");

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = new CommandLine(new ConnectAddCommand(new StubUserService(), connectService, prompt)).execute();

            assertEquals(0, exitCode);
            assertEquals("local", connectService.saved.getDbAlias());
            assertEquals(5432, connectService.saved.getDbPort());
            assertEquals("testdb", connectService.saved.getDbName());
            assertTrue(output.standardOutput().contains("저장되었습니다."));
        }
    }

    @Test
    void Connect_List는_조회한_DB목록을_출력한다() {
        StubConnectService service = new StubConnectService();
        service.configs = List.of(dbConfig());

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = new CommandLine(new ConnectListCommand(service)).execute();

            assertEquals(0, exitCode);
            assertTrue(output.standardOutput().contains("local"));
            assertTrue(output.standardOutput().contains("testdb"));
            assertFalse(output.standardOutput().contains("password"));
        }
    }

    @Test
    void Connect_Show는_Alias로_조회한_DB정보를_출력한다() {
        StubConnectService service = new StubConnectService();
        service.found = dbConfig();

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = new CommandLine(new ConnectShowCommand(service)).execute("local");

            assertEquals(0, exitCode);
            assertEquals("local", service.requestedAlias);
            assertTrue(output.standardOutput().contains("testdb"));
            assertTrue(output.standardOutput().contains("DB Password: ********"));
            assertFalse(output.standardOutput().contains("password"));
            assertFalse(output.errorOutput().contains("password"));
        }
    }

    @Test
    void Connect_Del은_Alias를_비활성화_Service에_전달한다() {
        StubConnectService service = new StubConnectService();

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = new CommandLine(new ConnectDelCommand(service)).execute("local");

            assertEquals(0, exitCode);
            assertEquals("local", service.deleted.getDbAlias());
            assertTrue(output.standardOutput().contains("비활성화"));
        }
    }

    @Test
    void Connect_Edit에서_빈값은_기존값을_유지한다() {
        StubConnectService service = new StubConnectService();
        service.found = dbConfig();
        PromptHelper prompt = prompt("\n1\n\n\n\n\n\n\n\n");

        try (ConsoleOutputCapture ignored = new ConsoleOutputCapture()) {
            int exitCode = new CommandLine(new ConnectEditCommand(service, prompt)).execute("local");

            assertEquals(0, exitCode);
            assertEquals("local", service.updated.getDbAlias());
            assertEquals("localhost", service.updated.getDbHost());
            assertEquals(5432, service.updated.getDbPort());
            assertEquals("password", service.updated.getDbPassword());
        }
    }

    private PromptHelper prompt(String input) {
        return new PromptHelper(new ByteArrayInputStream(input.getBytes(UTF_8)));
    }

    private DbConfig dbConfig() {
        DbConfig config = new DbConfig(100L, "local", "POSTGRESQL", "localhost", 5432, "testdb", "public", "tester", "password", 1);
        config.setCreatedAt("2026-01-01 00:00:00");
        config.setUpdateAt("2026-01-01 00:00:00");
        return config;
    }

    private static class StubUserService extends UserService {
        private StubUserService() {
            super(new UserRepository());
        }

        @Override
        public void checkUserExists() {
        }
    }

    private static class StubConnectService extends ConnectService {
        private DbConfig saved;
        private DbConfig updated;
        private DbConfig deleted;
        private DbConfig found;
        private List<DbConfig> configs;
        private String requestedAlias;

        private StubConnectService() {
            super(new DbConfigRepository());
        }

        @Override
        public void validateNewAlias(String alias) {
        }

        @Override
        public void saveDbConnect(DbConfig dbConfig) {
            saved = dbConfig;
        }

        @Override
        public void updateDbConfig(DbConfig dbConfig) {
            updated = dbConfig;
        }

        @Override
        public void deleteDbConfig(DbConfig dbConfig) {
            deleted = dbConfig;
        }

        @Override
        public DbConfig findDbConfig(String dbAlias) {
            requestedAlias = dbAlias;
            return found;
        }

        @Override
        public List<DbConfig> findDbConfigList() {
            return configs;
        }
    }
}
