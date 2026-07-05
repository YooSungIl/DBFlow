package io.dbflow.command.connect;

import io.dbflow.application.ConnectService;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.common.console.PromptHelper;
import io.dbflow.common.validation.CommonValidation;
import io.dbflow.domain.DbConfig;
import io.dbflow.infrastructure.repository.DbConfigRepository;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.util.List;

@Command(
        name = "edit",
        description = "DB설정 정보 변경"
)
public class ConnectEditCommand implements Runnable {

    @Parameters(
            index = "0",
            description = "DB Alias"
    )
    private String dbAlias;

    @Override
    public void run() {
        try {
            ConnectService connectService = new ConnectService(new DbConfigRepository());
            DbConfig beforDbConfig = connectService.findDbConfig(dbAlias);

            String dbAlias = PromptHelper.inputEdit("DB 별칭", connectService::validateNewAlias, beforDbConfig.getDbAlias());
            String dbType = PromptHelper.inputSelect("DB 종류", List.of("POSTGRESQL"));
            String dbHost = PromptHelper.inputEdit("Host", CommonValidation::required, beforDbConfig.getDbHost());
            Integer dbPort = PromptHelper.inputEditInt("Port", CommonValidation::validatePort, beforDbConfig.getDbPort());
            String dbName = PromptHelper.inputEdit("DB Name", CommonValidation::required, beforDbConfig.getDbName());
            String dbSchema = PromptHelper.inputEdit("DB Schema", CommonValidation::required, beforDbConfig.getDbSchema());
            String dbUser = PromptHelper.inputEdit("DB User", CommonValidation::required, beforDbConfig.getDbUser());
            String dbPassword = PromptHelper.inputEdit("DB Password", CommonValidation::required, beforDbConfig.getDbPassword());
            Integer useYn = PromptHelper.inputEditInt("DB useYn", CommonValidation::requiredInt, beforDbConfig.getUseYn());

            DbConfig dbConfig = new DbConfig(beforDbConfig.getDbConfigId(), dbAlias, dbType, dbHost, dbPort, dbName, dbSchema, dbUser, dbPassword, useYn);

            connectService.updateDbConfig(dbConfig);

            ConsoleHelper.success("DB접속 정보가 성공적으로 업데이트 되었습니다.");
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }
    }
}
