package io.dbflow.command.init;

import io.dbflow.application.InstallService;
import io.dbflow.common.console.ConsoleHelper;
import io.dbflow.common.enums.CommandExitCode;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
        name = "install",
        description = "DBFlow 사용자 데이터와 로컬 저장소를 초기 설치합니다."
)
public class InstallCommand implements Callable<Integer> {

    private final InstallService installService;

    public InstallCommand() {
        this(new InstallService());
    }

    public InstallCommand(InstallService installService) {
        this.installService = installService;
    }

    @Override
    public Integer call() {
        installService.install();
        ConsoleHelper.success("DBFlow 사용자 데이터 디렉터리를 생성했습니다.");
        return CommandExitCode.SUCCESS.getValue();
    }
}
