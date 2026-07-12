package io.dbflow;

import io.dbflow.common.enums.CommandExitCode;
import io.dbflow.testsupport.ConsoleOutputCapture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DBFlowApplicationTest {

    @Test
    void 도움말은_종료코드_0을_반환한다() {
        try (ConsoleOutputCapture ignored = new ConsoleOutputCapture()) {
            int exitCode = DBFlowApplication.createCommandLine().execute("--help");

            assertEquals(CommandExitCode.SUCCESS.getValue(), exitCode);
        }
    }

    @Test
    void 필수_인자가_없으면_종료코드_2를_반환한다() {
        try (ConsoleOutputCapture ignored = new ConsoleOutputCapture()) {
            int exitCode = DBFlowApplication.createCommandLine().execute("commit", "show");

            assertEquals(CommandExitCode.USAGE_ERROR.getValue(), exitCode);
        }
    }
}
