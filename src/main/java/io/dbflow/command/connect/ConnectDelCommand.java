package io.dbflow.command.connect;

import io.dbflow.application.ConnectService;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.domain.DbConfig;
import io.dbflow.infrastructure.repository.DbConfigRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
        name = "del",
        description = "DB설정 정보 삭제(비활성화)"
)
public class ConnectDelCommand implements Runnable {

    @Parameters(
            index = "0",
            description = "DB Alias"
    )
    private String dbAlias;

    @Override
    public void run() {
        try {
            ConnectService connectService = new ConnectService(new DbConfigRepository());

            DbConfig dbConfig = new DbConfig(dbAlias);
            connectService.deleteDbConfig(dbConfig);

            ConsoleHelper.success("'" + dbAlias + "' 비활성화 처리가 되었습니다.");
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }
    }
}
