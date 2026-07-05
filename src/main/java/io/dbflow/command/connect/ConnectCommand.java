package io.dbflow.command.connect;

import picocli.CommandLine.Command;

@Command(
        name = "connect",
        description = "DB연결 및 조회(스키마 기준으로 등록)",
        subcommands = {
                ConnectAddCommand.class,
                ConnectListCommand.class,
                ConnectShowCommand.class,
                ConnectDelCommand.class,
                ConnectEditCommand.class
        }
)
public class ConnectCommand {
}
