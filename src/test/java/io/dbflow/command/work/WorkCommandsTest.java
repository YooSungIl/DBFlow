package io.dbflow.command.work;

import io.dbflow.application.WorkService;
import io.dbflow.domain.Work;
import io.dbflow.testsupport.ConsoleOutputCapture;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkCommandsTest {

    @Test
    void Work_Set은_Alias를_Service에_전달한다() {
        StubWorkService service = new StubWorkService();

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = new CommandLine(new WorkSetCommand(service)).execute("local");

            assertEquals(0, exitCode);
            assertEquals("local", service.alias);
            assertTrue(output.standardOutput().contains("local"));
        }
    }

    @Test
    void Work_Show는_현재_Work를_출력한다() {
        StubWorkService service = new StubWorkService();
        service.work = work();

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = new CommandLine(new WorkShowCommand(service)).execute();

            assertEquals(0, exitCode);
            assertTrue(output.standardOutput().contains("local"));
            assertTrue(output.standardOutput().contains("testdb"));
        }
    }

    @Test
    void Work_Del은_해제_Service를_호출한다() {
        StubWorkService service = new StubWorkService();

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = new CommandLine(new WorkDelCommand(service)).execute();

            assertEquals(0, exitCode);
            assertTrue(service.deleted);
            assertTrue(output.standardOutput().contains("해제되었습니다."));
        }
    }

    private Work work() {
        Work work = new Work();
        work.setUserName("tester");
        work.setUserEmail("tester@example.com");
        work.setDbAlias("local");
        work.setDbType("POSTGRESQL");
        work.setDbHost("localhost");
        work.setDbPort(5432);
        work.setDbName("testdb");
        work.setDbSchema("public");
        return work;
    }

    private static class StubWorkService extends WorkService {
        private String alias;
        private Work work;
        private boolean deleted;

        @Override
        public void setWork(String alias) {
            this.alias = alias;
        }

        @Override
        public Work showWork() {
            return work;
        }

        @Override
        public void delWork() {
            deleted = true;
        }
    }
}
