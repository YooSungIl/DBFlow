package io.dbflow;

import io.dbflow.command.DbFlowCommand;
import picocli.CommandLine;

public class DBFlowApplication {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new DbFlowCommand()).execute(args);
        System.exit(exitCode);
    }
}