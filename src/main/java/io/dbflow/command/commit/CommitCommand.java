package io.dbflow.command.commit;


import io.dbflow.application.CommitService;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.common.console.PromptHelper;
import io.dbflow.common.validation.CommonValidation;
import picocli.CommandLine.Command;

@Command(
        name = "commit",
        description = "작업 변경내역을 커밋합니다.",
        subcommands = {
                CommitListCommand.class,
                CommitShowCommand.class
        }
)
public class CommitCommand implements Runnable {

    private final CommitService commitService = new CommitService();

    @Override
    public void run() {
        try {
            String title = PromptHelper.inputRequired("Commit title", CommonValidation::required);
            String content = PromptHelper.inputMultiLine("Commit description");

            commitService.commit(title, content);
            ConsoleHelper.success("커밋이 완료되었습니다.");
            ConsoleHelper.info("Commit title : " + title);
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }
    }
}
