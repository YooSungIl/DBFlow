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

    private final WorkService workService;

    public WorkShowCommand() {
        this(ServiceFactory.workService());
    }

    public WorkShowCommand(WorkService workService) {
        this.workService = workService;
    }

    @Override
    public void run() {
        Work work = workService.showWork();

        ConsoleHelper.printWork(work);
    }
}
