package io.dbflow.command;

import io.dbflow.command.commit.CommitCommand;
import io.dbflow.command.connect.ConnectCommand;
import io.dbflow.command.diff.DiffCommand;
import io.dbflow.command.init.InstallCommand;
import io.dbflow.command.user.UserCommand;
import io.dbflow.command.work.WorkCommand;
import picocli.CommandLine.Command;

@Command(
        name = "dbf",
        mixinStandardHelpOptions = true, // 옵션 명령어 줄임
        versionProvider = DbFlowVersionProvider.class,
        subcommands = {
                InstallCommand.class,
                UserCommand.class,
                ConnectCommand.class,
                WorkCommand.class,
                DiffCommand.class,
                CommitCommand.class
        }
)
public class DbFlowCommand {
}
