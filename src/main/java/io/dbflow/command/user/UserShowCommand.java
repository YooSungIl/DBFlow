package io.dbflow.command.user;

import io.dbflow.application.ServiceFactory;
import io.dbflow.application.UserService;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.common.console.PromptHelper;
import io.dbflow.common.validation.CommonValidation;
import io.dbflow.domain.User;
import picocli.CommandLine;

@CommandLine.Command(
        name = "show",
        description = "사용자 정보 조회"
)
public class UserShowCommand implements Runnable {

    private final UserService userService;
    private final PromptHelper promptHelper;

    public UserShowCommand() {
        this(ServiceFactory.userService(), new PromptHelper());
    }

    public UserShowCommand(UserService userService) {
        this(userService, new PromptHelper());
    }

    public UserShowCommand(UserService userService, PromptHelper promptHelper) {
        this.userService = userService;
        this.promptHelper = promptHelper;
    }

    @Override
    public void run() {
        User user = userService.findActiveUser();
        if (user == null) {
            ConsoleHelper.info("등록된 사용자 정보가 없습니다.");
            ConsoleHelper.info("사용자 정보를 먼저 등록합니다.");

            String name = promptHelper.inputRequired("사용자명", CommonValidation::required);
            String email = promptHelper.inputRequired("이메일", CommonValidation::validateEmail);

            userService.saveUserConfig(name, email);
            ConsoleHelper.success("사용자 정보가 저장되었습니다.");

            user = userService.findActiveUser();
        }

        ConsoleHelper.printUser(user);
    }
}
