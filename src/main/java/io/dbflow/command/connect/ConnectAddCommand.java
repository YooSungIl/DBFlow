package io.dbflow.command.connect;

import io.dbflow.application.ConnectService;
import io.dbflow.application.ServiceFactory;
import io.dbflow.application.UserService;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.common.console.PromptHelper;
import io.dbflow.common.validation.CommonValidation;
import io.dbflow.domain.DbConfig;
import picocli.CommandLine.Command;

import java.util.List;

@Command(
        name = "add",
        description = "DB설정 정보 추가"
)
public class ConnectAddCommand implements Runnable {

    private final UserService userService;
    private final ConnectService connectService;
    private final PromptHelper promptHelper;

    public ConnectAddCommand() {
        this(ServiceFactory.userService(), ServiceFactory.connectService(), new PromptHelper());
    }

    public ConnectAddCommand(UserService userService, ConnectService connectService) {
        this(userService, connectService, new PromptHelper());
    }

    public ConnectAddCommand(UserService userService, ConnectService connectService, PromptHelper promptHelper) {
        this.userService = userService;
        this.connectService = connectService;
        this.promptHelper = promptHelper;
    }

    @Override
    public void run() {

        try {
            userService.checkUserExists();

            String dbAlias = promptHelper.inputRequired("DB별칭", connectService::validateNewAlias);
            String dbType = promptHelper.inputSelect("지원하는 DBMS종류를 선택해 주세요.", List.of("POSTGRESQL"));
            String dbHost = promptHelper.inputRequired("Host", CommonValidation::required);
            Integer dbPort = promptHelper.inputRequiredInt("Port", CommonValidation::validatePort);
            String dbName = promptHelper.inputRequired("Database", CommonValidation::required);
            String dbSchema = promptHelper.inputRequired("Schema", CommonValidation::required);
            String dbUser = promptHelper.inputRequired("UserName", CommonValidation::required);
            String dbPassword = promptHelper.inputRequired("Password", CommonValidation::required);

            DbConfig dbConfig = new DbConfig(dbAlias, dbType, dbHost, dbPort, dbName, dbSchema, dbUser, dbPassword);


            connectService.saveDbConnect(dbConfig);

            ConsoleHelper.success("DB접속 정보가 저장되었습니다.");
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }
    }
}
