package io.dbflow.command.user;

import picocli.CommandLine.Command;

@Command(
        name = "user",
        description = "사용자 정보 등록 및 조회",
        subcommands = {
                UserSetCommand.class,
                UserShowCommand.class
        }
)
public class UserCommand {
}