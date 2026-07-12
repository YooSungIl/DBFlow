package io.dbflow.command.work;

import io.dbflow.application.ServiceFactory;
import io.dbflow.application.WorkService;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.common.console.PromptHelper;
import io.dbflow.common.validation.CommonValidation;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
        name = "set",
        description = "DB작업 공간 지정"
)
public class WorkSetCommand implements Runnable {

    private final WorkService workService;
    private final PromptHelper promptHelper;

    public WorkSetCommand() {
        this(ServiceFactory.workService(), new PromptHelper());
    }

    public WorkSetCommand(WorkService workService) {
        this(workService, new PromptHelper());
    }

    public WorkSetCommand(WorkService workService, PromptHelper promptHelper) {
        this.workService = workService;
        this.promptHelper = promptHelper;
    }

    @Parameters(
            index = "0",
            description = "DB Alias"
    )
    private String dbAlias;

    @Override
    public void run() {
        if (dbAlias == null || dbAlias.isBlank()) {
            dbAlias = promptHelper.inputRequired("DB별칭", CommonValidation::required);
        }

        workService.setWork(dbAlias);
        ConsoleHelper.success("DB작업 공간이 " + "'" + dbAlias + "'으로 변경 되었습니다.");
    }
}
