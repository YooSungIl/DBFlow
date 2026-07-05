package io.dbflow.command.user;

import io.dbflow.application.UserService;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.common.console.PromptHelper;
import io.dbflow.common.validation.CommonValidation;
import io.dbflow.infrastructure.repository.UserRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "set",
        description = "사용자 정보 등록 및 수정"
)
public class UserSetCommand implements Runnable {

    @Option(names = "--name")
    private String name;

    @Option(names = "--email")
    private String email;

    @Override
    public void run() {

        try {
            if (name == null || name.isBlank()) {
                name = PromptHelper.inputRequired("사용자명", CommonValidation::required);
            }

            if (email == null || email.isBlank()) {
                email = PromptHelper.inputRequired("이메일", CommonValidation::validateEmail);
            }

            UserService userService = new UserService(new UserRepository());
            userService.saveUserConfig(name, email);

            ConsoleHelper.success("사용자 정보가 저장되었습니다.");
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }
    }
}