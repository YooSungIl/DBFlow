package io.dbflow.command.connect;

import io.dbflow.application.ConnectService;
import io.dbflow.application.UserService;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.common.console.PromptHelper;
import io.dbflow.common.validation.CommonValidation;
import io.dbflow.domain.DbConfig;
import io.dbflow.infrastructure.repository.DbConfigRepository;
import io.dbflow.infrastructure.repository.UserRepository;
import picocli.CommandLine.Command;

import java.util.List;

@Command(
        name = "add",
        description = "DB설정 정보 추가"
)
public class ConnectAddCommand implements Runnable {

    @Override
    public void run() {

        try {
            UserService userService = new UserService(new UserRepository());
            userService.checkUserExists();

            ConnectService connectService = new ConnectService(new DbConfigRepository());

            String dbAlias = PromptHelper.inputRequired("DB별칭", connectService::validateNewAlias);
            String dbType = PromptHelper.inputSelect("지원하는 DBMS종류를 선택해 주세요.", List.of("POSTGRESQL"));
            String dbHost = PromptHelper.inputRequired("Host", CommonValidation::required);
            Integer dbPort = PromptHelper.inputRequiredInt("Port", CommonValidation::validatePort);
            String dbName = PromptHelper.inputRequired("Database", CommonValidation::required);
            String dbSchema = PromptHelper.inputRequired("Schema", CommonValidation::required);
            String dbUser = PromptHelper.inputRequired("UserName", CommonValidation::required);
            String dbPassword = PromptHelper.inputRequired("Password", CommonValidation::required);

            DbConfig dbConfig = new DbConfig(dbAlias, dbType, dbHost, dbPort, dbName, dbSchema, dbUser, dbPassword);


            connectService.saveDbConnect(dbConfig);

            ConsoleHelper.success("DB접속 정보가 저장되었습니다.");
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }
    }
}
