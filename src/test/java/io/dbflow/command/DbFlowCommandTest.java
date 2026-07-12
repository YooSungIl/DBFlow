package io.dbflow.command;

import io.dbflow.DBFlowApplication;
import io.dbflow.common.enums.CommandExitCode;
import io.dbflow.testsupport.ConsoleOutputCapture;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbFlowCommandTest {

    @Test
    void 루트에_필수_하위_명령이_등록되어_있다() {
        Map<String, CommandLine> subcommands = DBFlowApplication.createCommandLine().getSubcommands();

        assertAll(
                () -> assertTrue(subcommands.containsKey("user")),
                () -> assertTrue(subcommands.containsKey("connect")),
                () -> assertTrue(subcommands.containsKey("work")),
                () -> assertTrue(subcommands.containsKey("diff")),
                () -> assertTrue(subcommands.containsKey("commit"))
        );
    }

    @Test
    void 명령_그룹별_하위_명령이_등록되어_있다() {
        Map<String, CommandLine> rootCommands = DBFlowApplication.createCommandLine().getSubcommands();

        Map<String, CommandLine> userCommands = rootCommands.get("user").getSubcommands();
        Map<String, CommandLine> connectCommands = rootCommands.get("connect").getSubcommands();
        Map<String, CommandLine> workCommands = rootCommands.get("work").getSubcommands();
        Map<String, CommandLine> commitCommands = rootCommands.get("commit").getSubcommands();

        assertAll(
                () -> assertTrue(userCommands.keySet().containsAll(List.of("set", "show"))),
                () -> assertTrue(connectCommands.keySet().containsAll(List.of("add", "list", "show", "edit", "del"))),
                () -> assertTrue(workCommands.keySet().containsAll(List.of("set", "show", "del"))),
                () -> assertTrue(commitCommands.keySet().containsAll(List.of("list", "show")))
        );
    }

    @Test
    void 도움말은_성공_종료하고_사용법을_출력한다() {
        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = DBFlowApplication.createCommandLine().execute("--help");

            assertEquals(CommandExitCode.SUCCESS.getValue(), exitCode);
            assertTrue(output.standardOutput().contains("Usage:"));
        }
    }

    @Test
    void 버전은_성공_종료하고_현재_버전을_출력한다() {
        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = DBFlowApplication.createCommandLine().execute("--version");

            assertEquals(CommandExitCode.SUCCESS.getValue(), exitCode);
            assertTrue(output.standardOutput().contains("0.0.1"));
        }
    }

    @Test
    void 등록되지_않은_명령은_사용법_오류를_반환한다() {
        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = DBFlowApplication.createCommandLine().execute("unknown");

            assertEquals(CommandExitCode.USAGE_ERROR.getValue(), exitCode);
            assertTrue(output.errorOutput().contains("unknown"));
        }
    }
}
