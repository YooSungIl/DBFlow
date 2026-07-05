package io.dbflow.command.work;

import picocli.CommandLine.Command;

@Command(
        name = "work",
        description = "DB작업 환경 지정",
        subcommands = {
                WorkSetCommand.class,
                WorkShowCommand.class,
                WorkDelCommand.class
        }
)
public class WorkCommand {
}
