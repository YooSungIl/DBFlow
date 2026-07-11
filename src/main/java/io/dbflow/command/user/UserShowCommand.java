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

    @Override
    public void run() {
        try {
            UserService userService = ServiceFactory.userService();
            User user = userService.findActiveUser();
            if (user == null) {
                ConsoleHelper.info("등록된 사용자 정보가 없습니다.");
                ConsoleHelper.info("사용자 정보를 먼저 등록합니다.");

                String name = PromptHelper.inputRequired("사용자명", CommonValidation::required);
                String email = PromptHelper.inputRequired("이메일", CommonValidation::validateEmail);

                userService.saveUserConfig(name, email);
                ConsoleHelper.success("사용자 정보가 저장되었습니다.");

                user = userService.findActiveUser();
            }

            System.out.println();
            System.out.println("이름 : " + user.getUserName());
            System.out.println("이메일 : " + user.getUserEmail());
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }
    }
}
