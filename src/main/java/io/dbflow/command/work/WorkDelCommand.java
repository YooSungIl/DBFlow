package io.dbflow.command.work;

import io.dbflow.application.ServiceFactory;
import io.dbflow.application.WorkService;
import io.dbflow.common.console.ConsoleHelper;
import picocli.CommandLine.Command;

@Command(
        name = "del",
        description = "DB작업 공간 해제"
)
public class WorkDelCommand implements Runnable {

    private final WorkService workService;

    public WorkDelCommand() {
        this(ServiceFactory.workService());
    }

    public WorkDelCommand(WorkService workService) {
        this.workService = workService;
    }

    @Override
    public void run() {
        workService.clearWork();
        ConsoleHelper.success("DB작업 공간이 정상적으로 해제되었습니다.");
    }
}
