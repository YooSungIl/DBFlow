package io.dbflow.command.connect;

import io.dbflow.application.ConnectService;
import io.dbflow.application.ServiceFactory;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.domain.DbConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
        name = "del",
        description = "DB설정 정보 삭제(비활성화)"
)
public class ConnectDelCommand implements Runnable {

    private final ConnectService connectService;

    public ConnectDelCommand() {
        this(ServiceFactory.connectService());
    }

    public ConnectDelCommand(ConnectService connectService) {
        this.connectService = connectService;
    }

    @Parameters(
            index = "0",
            description = "DB Alias"
    )
    private String dbAlias;

    @Override
    public void run() {
        DbConfig dbConfig = new DbConfig(dbAlias);
        connectService.deleteDbConfig(dbConfig);
        ConsoleHelper.success("'" + dbAlias + "' 비활성화 처리가 되었습니다.");
    }
}
