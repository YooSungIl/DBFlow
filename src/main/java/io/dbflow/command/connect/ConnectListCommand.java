package io.dbflow.command.connect;

import io.dbflow.application.ConnectService;
import io.dbflow.application.ServiceFactory;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.domain.DbConfig;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(
        name = "list",
        description = "DB설정 정보 목록 조회"
)
public class ConnectListCommand implements Runnable {

    private final ConnectService connectService;

    public ConnectListCommand() {
        this(ServiceFactory.connectService());
    }

    public ConnectListCommand(ConnectService connectService) {
        this.connectService = connectService;
    }

    @Override
    public void run() {
        try {
            List<DbConfig> dbConfig = connectService.findDbConfigList();

            if (dbConfig == null) {
                ConsoleHelper.info("등록된 DB접속 정보가 없습니다.");
            } else {
                ConsoleHelper.printDbConfigList(dbConfig);
            }
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }
    }
}
