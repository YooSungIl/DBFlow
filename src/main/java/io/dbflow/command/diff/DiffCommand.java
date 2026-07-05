package io.dbflow.command.diff;

import io.dbflow.application.DiffService;
import io.dbflow.common.console.WorkDiffPrinter;
import io.dbflow.domain.WorkTarget;
import picocli.CommandLine.Command;

import java.util.List;

@Command(
        name = "diff",
        description = "DB오브젝트 작업 결과 비교"
)
public class DiffCommand implements Runnable {

    @Override
    public void run() {
        DiffService diffService = new DiffService();
        List<WorkTarget> targets = diffService.diff();

        WorkDiffPrinter.print(targets);
    }
}
