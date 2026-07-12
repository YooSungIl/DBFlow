package io.dbflow.command.commit;


import io.dbflow.application.CommitService;
import io.dbflow.application.ServiceFactory;
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

    private final CommitService commitService;
    private final PromptHelper promptHelper;

    public CommitCommand() {
        this(ServiceFactory.commitService(), new PromptHelper());
    }

    public CommitCommand(CommitService commitService) {
        this(commitService, new PromptHelper());
    }

    public CommitCommand(CommitService commitService, PromptHelper promptHelper) {
        this.commitService = commitService;
        this.promptHelper = promptHelper;
    }

    @Override
    public void run() {
        try {
            String title = promptHelper.inputRequired("Commit title", CommonValidation::required);
            String content = promptHelper.inputMultiLine("Commit description");

            commitService.commit(title, content);
            ConsoleHelper.success("커밋이 완료되었습니다.");
            ConsoleHelper.info("Commit title : " + title);
        } catch (Exception e) {
            ConsoleHelper.error(e.getMessage());
        }
    }
}
