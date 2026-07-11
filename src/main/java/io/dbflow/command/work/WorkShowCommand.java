package io.dbflow.command.work;

import io.dbflow.application.ServiceFactory;
import io.dbflow.application.WorkService;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.domain.Work;
import picocli.CommandLine.Command;

@Command(
        name = "show",
        description = "DB작업 공간 확인"
)
public class WorkShowCommand implements Runnable {

    @Override
    public void run() {
        try {
            WorkService workService = ServiceFactory.workService();
            Work work = workService.showWork();

            System.out.println();
            System.out.println("이름 : " + work.getUserName());
            System.out.println("이메일 : " + work.getUserEmail());
            System.out.println("DB별칭 : " + work.getDbAlias());
            System.out.println("DB종류 : " + work.getDbType());
            System.out.println("DBHost : " + work.getDbHost());
            System.out.println("DBPort : " + work.getDbPort());
            System.out.println("DB명 : " + work.getDbName());
            System.out.println("DBSchema : " + work.getDbSchema());
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }
    }
}
