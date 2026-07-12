package io.dbflow.command.connect;

import io.dbflow.application.ConnectService;
import io.dbflow.application.ServiceFactory;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.common.console.PromptHelper;
import io.dbflow.common.validation.CommonValidation;
import io.dbflow.domain.DbConfig;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.util.List;

@Command(
        name = "edit",
        description = "DB설정 정보 변경"
)
public class ConnectEditCommand implements Runnable {

    private final ConnectService connectService;
    private final PromptHelper promptHelper;

    public ConnectEditCommand() {
        this(ServiceFactory.connectService(), new PromptHelper());
    }

    public ConnectEditCommand(ConnectService connectService) {
        this(connectService, new PromptHelper());
    }

    public ConnectEditCommand(ConnectService connectService, PromptHelper promptHelper) {
        this.connectService = connectService;
        this.promptHelper = promptHelper;
    }

    @Parameters(
            index = "0",
            description = "DB Alias"
    )
    private String dbAlias;

    @Override
    public void run() {
        try {
            DbConfig beforDbConfig = connectService.findDbConfig(dbAlias);

            String dbAlias = promptHelper.inputEdit("DB 별칭", connectService::validateNewAlias, beforDbConfig.getDbAlias());
            String dbType = promptHelper.inputSelect("DB 종류", List.of("POSTGRESQL"));
            String dbHost = promptHelper.inputEdit("Host", CommonValidation::required, beforDbConfig.getDbHost());
            Integer dbPort = promptHelper.inputEditInt("Port", CommonValidation::validatePort, beforDbConfig.getDbPort());
            String dbName = promptHelper.inputEdit("DB Name", CommonValidation::required, beforDbConfig.getDbName());
            String dbSchema = promptHelper.inputEdit("DB Schema", CommonValidation::required, beforDbConfig.getDbSchema());
            String dbUser = promptHelper.inputEdit("DB User", CommonValidation::required, beforDbConfig.getDbUser());
            String dbPassword = promptHelper.inputEdit("DB Password", CommonValidation::required, beforDbConfig.getDbPassword());
            Integer useYn = promptHelper.inputEditInt("DB useYn", CommonValidation::requiredInt, beforDbConfig.getUseYn());

            DbConfig dbConfig = new DbConfig(beforDbConfig.getDbConfigId(), dbAlias, dbType, dbHost, dbPort, dbName, dbSchema, dbUser, dbPassword, useYn);

            connectService.updateDbConfig(dbConfig);

            ConsoleHelper.success("DB접속 정보가 성공적으로 업데이트 되었습니다.");
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }
    }
}
