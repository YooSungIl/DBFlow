package io.dbflow.command.commit;

import io.dbflow.application.CommitService;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.dto.CommitLogView;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

@Command(
        name = "list",
        description = "커밋 목록 조회"
)
public class CommitListCommand implements Runnable {

    @Parameters(
            index = "0",
            arity = "0..1",
            description = "조회할 커밋 개수",
            defaultValue = "20"
    )
    private int limit;

    @Override
    public void run()  {
        try {
            CommitService commitService = new CommitService();

            List<CommitLogView> commitLogList = commitService.commitLogList(limit);
            if (commitLogList == null || commitLogList.isEmpty()) {
                ConsoleHelper.info("등록된 커밋 정보가 없습니다.");
            } else {
                ConsoleHelper.printCommitLogList(commitLogList);
            }
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }
    }
}
