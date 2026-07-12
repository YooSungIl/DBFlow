package io.dbflow.command.user;

import io.dbflow.application.ServiceFactory;
import io.dbflow.application.UserService;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.common.console.PromptHelper;
import io.dbflow.common.validation.CommonValidation;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "set",
        description = "사용자 정보 등록 및 수정"
)
public class UserSetCommand implements Runnable {

    private final UserService userService;
    private final PromptHelper promptHelper;

    public UserSetCommand() {
        this(ServiceFactory.userService(), new PromptHelper());
    }

    public UserSetCommand(UserService userService) {
        this(userService, new PromptHelper());
    }

    public UserSetCommand(UserService userService, PromptHelper promptHelper) {
        this.userService = userService;
        this.promptHelper = promptHelper;
    }


    @Option(names = "--name")
    private String name;

    @Option(names = "--email")
    private String email;

    @Override
    public void run() {

        try {
            if (name == null || name.isBlank()) {
                name = promptHelper.inputRequired("사용자명", CommonValidation::required);
            }

            if (email == null || email.isBlank()) {
                email = promptHelper.inputRequired("이메일", CommonValidation::validateEmail);
            }

            userService.saveUserConfig(name, email);

            ConsoleHelper.success("사용자 정보가 저장되었습니다.");
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }
    }
}
