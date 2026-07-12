package io.dbflow.common.Exception;

import io.dbflow.common.enums.CommandExitCode;
import io.dbflow.testsupport.ConsoleOutputCapture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandExceptionHandlerTest {

    private final CommandExceptionHandler exceptionHandler = new CommandExceptionHandler();

    @Test
    void DBFlow_예외는_기존_메시지와_종료코드_1을_반환한다() {
        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = exceptionHandler.handleExecutionException(
                    new ServiceException("등록된 사용자 정보가 없습니다."),
                    null,
                    null
            );

            assertEquals(CommandExitCode.EXECUTION_ERROR.getValue(), exitCode);
            assertTrue(output.errorOutput().contains("등록된 사용자 정보가 없습니다."));
            assertTrue(output.standardOutput().isEmpty());
        }
    }

    @Test
    void 예상하지_못한_예외는_기본_오류_메시지를_표시한다() {
        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = exceptionHandler.handleExecutionException(
                    new IllegalStateException("내부 상세 오류"),
                    null,
                    null
            );

            assertEquals(CommandExitCode.EXECUTION_ERROR.getValue(), exitCode);
            assertTrue(output.errorOutput().contains(CommandExceptionHandler.DEFAULT_ERROR_MESSAGE));
            assertFalse(output.errorOutput().contains("내부 상세 오류"));
        }
    }
}
