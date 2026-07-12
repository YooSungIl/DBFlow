package io.dbflow.common.Exception;

import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.common.enums.CommandExitCode;
import picocli.CommandLine;

public class CommandExceptionHandler implements CommandLine.IExecutionExceptionHandler {

    public static final String DEFAULT_ERROR_MESSAGE = "명령 처리 중 오류가 발생했습니다.";

    @Override
    public int handleExecutionException(
            Exception exception,
            CommandLine commandLine,
            CommandLine.ParseResult parseResult
    ) {
        ConsoleHelper.error(resolveMessage(exception));
        return CommandExitCode.EXECUTION_ERROR.getValue();
    }

    private String resolveMessage(Exception exception) {
        if (exception instanceof ServiceException
                || exception instanceof RepositoryException
                || exception instanceof ValidationException) {
            String message = exception.getMessage();
            if (message != null && !message.isBlank()) {
                return message;
            }
        }

        return DEFAULT_ERROR_MESSAGE;
    }
}
