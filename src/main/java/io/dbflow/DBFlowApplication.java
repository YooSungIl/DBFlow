package io.dbflow;

import io.dbflow.command.DbFlowCommand;
import io.dbflow.common.Exception.CommandExceptionHandler;
import picocli.CommandLine;

public class DBFlowApplication {
    public static void main(String[] args) {
        int exitCode = createCommandLine().execute(args);
        System.exit(exitCode);
    }

    public static CommandLine createCommandLine() {
        CommandLine commandLine = new CommandLine(new DbFlowCommand());
        commandLine.setExecutionExceptionHandler(new CommandExceptionHandler());
        return commandLine;
    }
}
