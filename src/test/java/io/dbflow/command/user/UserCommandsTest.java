package io.dbflow.command.user;

import io.dbflow.application.UserService;
import io.dbflow.common.console.PromptHelper;
import io.dbflow.domain.User;
import io.dbflow.infrastructure.repository.UserRepository;
import io.dbflow.testsupport.ConsoleOutputCapture;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserCommandsTest {

    @Test
    void User_Set은_옵션값을_Service에_전달한다() {
        StubUserService service = new StubUserService();

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = new CommandLine(new UserSetCommand(service)).execute("--name", "tester", "--email", "tester@example.com");

            assertEquals(0, exitCode);
            assertEquals("tester", service.savedName);
            assertEquals("tester@example.com", service.savedEmail);
            assertTrue(output.standardOutput().contains("저장되었습니다."));
        }
    }

    @Test
    void User_Show는_사용자가_없으면_입력받아_저장하고_출력한다() {
        StubUserService service = new StubUserService();
        PromptHelper prompt = new PromptHelper(new ByteArrayInputStream("tester\ntester@example.com\n".getBytes(UTF_8)));

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = new CommandLine(new UserShowCommand(service, prompt)).execute();

            assertEquals(0, exitCode);
            assertEquals("tester", service.savedName);
            assertTrue(output.standardOutput().contains("tester@example.com"));
        }
    }

    private static class StubUserService extends UserService {
        private User user;
        private String savedName;
        private String savedEmail;

        private StubUserService() {
            super(new UserRepository());
        }

        @Override
        public User findActiveUser() {
            return user;
        }

        @Override
        public void saveUserConfig(String name, String email) {
            savedName = name;
            savedEmail = email;
            user = new User();
            user.setUserName(name);
            user.setUserEmail(email);
        }
    }
}
