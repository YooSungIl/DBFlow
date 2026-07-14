package io.dbflow.command.init;

import io.dbflow.application.InstallService;
import io.dbflow.common.enums.CommandExitCode;
import io.dbflow.testsupport.ConsoleOutputCapture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstallCommandTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void Install은_사용자_디렉터리를_생성하고_성공_메시지를_출력한다() {
        Path userDataDirectory = temporaryDirectory.resolve(".dbflow");
        InstallCommand command = new InstallCommand(new InstallService(userDataDirectory));

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = new CommandLine(command).execute();

            assertEquals(CommandExitCode.SUCCESS.getValue(), exitCode);
            assertTrue(Files.isDirectory(userDataDirectory.resolve("data")));
            assertTrue(Files.isDirectory(userDataDirectory.resolve("security")));
            assertTrue(output.standardOutput().contains("사용자 데이터 디렉터리를 생성했습니다"));
        }
    }
}
