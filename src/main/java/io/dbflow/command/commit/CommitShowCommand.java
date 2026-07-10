package io.dbflow.command.commit;

import io.dbflow.application.CommitService;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.common.console.TablePrinter;
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
            CommitService commitService = new CommitService();
            CommitLogView commitLogView = new CommitLogView();

            if (objectName == null || objectName.isEmpty()) {
                commitLogView = commitService.commitTargetList(commitLogId);
                if (commitLogView == null) {
                    ConsoleHelper.info("등록된 커밋 정보가 없습니다.");
                } else {
                    ConsoleHelper.commitLogInfo(commitLogView);
                    TablePrinter.printCommitTargetList(commitLogView.getCommitTargetViewList());
                }
            } else if (componentName == null || componentName.isEmpty()) {
                commitLogView = commitService.commitObjectDetail(commitLogId, objectName);
                if (commitLogView.getCommitTargetViewList().isEmpty()) {
                    ConsoleHelper.info("등록된 오브젝트 정보가 없습니다.");
                } else {
                    ConsoleHelper.commitTargetInfo(commitLogView);
                }
            } else {
                commitLogView = commitService.commitComponentDetail(commitLogId, objectName, componentName);
                if (commitLogView.getCommitTargetViewList().get(0).getCommitComponentChangeViews().isEmpty()) {
                    ConsoleHelper.info("등록된 구성 요소 정보가 없습니다.");
                } else {
                    ConsoleHelper.commitComponentInfo(commitLogView);
                }
            }
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }

    }
}
