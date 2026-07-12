package io.dbflow.command.commit;

import io.dbflow.application.CommitService;
import io.dbflow.application.ServiceFactory;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.dto.CommitLogView;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "show",
        description = "커밋 목록 상세 조회"
)
public class CommitShowCommand implements Runnable {

    @Parameters(
            index = "0",
            description = "커밋ID"
    )
    private Long commitLogId;

    @Option(names = {"-object", "-o"}, description = "오브젝트명")
    private String objectName;

    @Option(names = {"-component", "-c"}, description = "항목명")
    private String componentName;

    @Override
    public void run() {
        try {
            CommitService commitService = ServiceFactory.commitService();
            if (objectName == null || objectName.isEmpty()) {
                CommitLogView commitLogView = commitService.commitTargetList(commitLogId);
                ConsoleHelper.commitLogInfo(commitLogView);
                ConsoleHelper.printCommitTargetList(commitLogView.getTargets());
            } else if (componentName == null || componentName.isEmpty()) {
                CommitLogView commitLogView = commitService.commitObjectDetail(commitLogId, objectName);
                ConsoleHelper.commitTargetInfo(commitLogView);
            } else {
                CommitLogView commitLogView = commitService.commitComponentDetail(commitLogId, objectName, componentName);
                ConsoleHelper.commitComponentInfo(commitLogView);
            }
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }

    }
}
