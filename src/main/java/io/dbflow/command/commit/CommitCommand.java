package io.dbflow.command.commit;


import io.dbflow.application.CommitService;
import io.dbflow.common.console.PromptHelper;
import io.dbflow.common.validation.CommonValidation;
import picocli.CommandLine.Command;

@Command(
        name = "commit",
        description = "작업 변경내역을 커밋합니다."
)
public class CommitCommand implements Runnable {

    private final CommitService commitService = new CommitService();

    @Override
    public void run() {
        String title = PromptHelper.inputRequired("Commit title", CommonValidation::required);
        String content = PromptHelper.inputMultiLine("Commit description");

        commitService.commit(title, content);
    }
}
