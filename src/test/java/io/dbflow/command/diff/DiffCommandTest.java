package io.dbflow.command.diff;

import io.dbflow.application.DiffService;
import io.dbflow.common.Exception.CommandExceptionHandler;
import io.dbflow.common.Exception.ServiceException;
import io.dbflow.common.enums.CommandExitCode;
import io.dbflow.domain.WorkTarget;
import io.dbflow.testsupport.ConsoleOutputCapture;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiffCommandTest {

    @Test
    void Diff_결과를_출력하고_정상_종료한다() {
        WorkTarget target = new WorkTarget(100L, "TABLE", "member", "회원", "ADD");
        CommandLine commandLine = commandLine(new DiffCommand(new StubDiffService(List.of(target), null)));

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = commandLine.execute();

            assertEquals(CommandExitCode.SUCCESS.getValue(), exitCode);
            assertTrue(output.standardOutput().contains("member"));
            assertTrue(output.standardOutput().contains("ADD"));
        }
    }

    @Test
    void Diff_실패는_오류를_출력하고_종료코드_1을_반환한다() {
        CommandLine commandLine = commandLine(new DiffCommand(new StubDiffService(List.of(), new ServiceException("Work가 없습니다."))));

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = commandLine.execute();

            assertEquals(CommandExitCode.EXECUTION_ERROR.getValue(), exitCode);
            assertTrue(output.errorOutput().contains("Work가 없습니다."));
        }
    }

    private CommandLine commandLine(DiffCommand command) {
        CommandLine commandLine = new CommandLine(command);
        commandLine.setExecutionExceptionHandler(new CommandExceptionHandler());
        return commandLine;
    }

    private static class StubDiffService extends DiffService {
        private final List<WorkTarget> targets;
        private final RuntimeException exception;

        private StubDiffService(List<WorkTarget> targets, RuntimeException exception) {
            this.targets = targets;
            this.exception = exception;
        }

        @Override
        public List<WorkTarget> diff() {
            if (exception != null) {
                throw exception;
            }
            return targets;
        }
    }
}
