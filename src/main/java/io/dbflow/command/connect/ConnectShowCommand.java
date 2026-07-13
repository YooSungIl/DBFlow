package io.dbflow.command.connect;

import io.dbflow.application.ConnectService;
import io.dbflow.application.ServiceFactory;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.domain.DbConfig;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

@Command(
        name = "show",
        description = "DB설정 정보 상세 조회"
)
public class ConnectShowCommand implements Runnable {

    private final ConnectService connectService;

    public ConnectShowCommand() {
        this(ServiceFactory.connectService());
    }

    public ConnectShowCommand(ConnectService connectService) {
        this.connectService = connectService;
    }

    @Parameters(
            index = "0",
            description = "DB Alias"
    )
    private String dbAlias;

    @Override
    public void run() {
        DbConfig dbConfig = connectService.findDbConfig(dbAlias);

        if (dbConfig == null) {
            ConsoleHelper.info("등록된 DB접속 정보가 없습니다.");
        } else {
            ConsoleHelper.printDbConfig(dbAlias, dbConfig);
        }
    }
}
