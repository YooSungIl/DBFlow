package io.dbflow.command.commit;

import io.dbflow.application.CommitService;
import io.dbflow.common.exception.CommandExceptionHandler;
import io.dbflow.common.exception.ServiceException;
import io.dbflow.common.console.PromptHelper;
import io.dbflow.common.enums.CommandExitCode;
import io.dbflow.dto.CommitChangeDetailView;
import io.dbflow.dto.CommitLogView;
import io.dbflow.dto.CommitTargetView;
import io.dbflow.testsupport.ConsoleOutputCapture;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommitCommandsTest {

    @Test
    void Commit은_입력한_제목과_내용을_Service에_전달한다() {
        StubCommitService service = new StubCommitService();
        CommitCommand command = new CommitCommand(service, prompt("제목\n첫 줄\n둘째 줄\n\n\n"));

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = commandLine(command).execute();

            assertEquals(CommandExitCode.SUCCESS.getValue(), exitCode);
            assertEquals("제목", service.commitTitle);
            assertEquals("첫 줄" + System.lineSeparator() + "둘째 줄", service.commitContent);
            assertTrue(output.standardOutput().contains("커밋이 완료되었습니다."));
        }
    }

    @Test
    void Commit_실패는_종료코드_1을_반환한다() {
        StubCommitService service = new StubCommitService();
        service.exception = new ServiceException("커밋할 변경내역이 없습니다.");
        CommitCommand command = new CommitCommand(service, prompt("제목\n\n\n"));

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = commandLine(command).execute();

            assertEquals(CommandExitCode.EXECUTION_ERROR.getValue(), exitCode);
            assertTrue(output.errorOutput().contains("커밋할 변경내역이 없습니다."));
        }
    }

    @Test
    void Commit_List는_기본값_20을_Service에_전달한다() {
        StubCommitService service = new StubCommitService();
        service.commitLogs = List.of();

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = commandLine(new CommitListCommand(service)).execute();

            assertEquals(CommandExitCode.SUCCESS.getValue(), exitCode);
            assertEquals(20, service.limit);
            assertTrue(output.standardOutput().contains("등록된 커밋 정보가 없습니다."));
        }
    }

    @Test
    void Commit_List는_입력한_조회개수를_Service에_전달한다() {
        StubCommitService service = new StubCommitService();
        service.commitLogs = List.of(commitView());

        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            int exitCode = commandLine(new CommitListCommand(service)).execute("5");

            assertEquals(CommandExitCode.SUCCESS.getValue(), exitCode);
            assertEquals(5, service.limit);
            assertTrue(output.standardOutput().contains("테스트 커밋"));
        }
    }

    @Test
    void Commit_Show는_옵션에_따라_조회_Service를_선택한다() {
        StubCommitService targetListService = new StubCommitService();
        targetListService.view = commitView();
        StubCommitService objectService = new StubCommitService();
        objectService.view = commitView();
        StubCommitService componentService = new StubCommitService();
        componentService.view = commitView();

        try (ConsoleOutputCapture ignored = new ConsoleOutputCapture()) {
            assertEquals(0, commandLine(new CommitShowCommand(targetListService)).execute("10"));
            assertEquals(0, commandLine(new CommitShowCommand(objectService)).execute("10", "-o", "member"));
            assertEquals(0, commandLine(new CommitShowCommand(componentService)).execute("10", "-o", "member", "-c", "member_id"));
        }

        assertEquals("targetList:10", targetListService.calledMethod);
        assertEquals("object:10:member", objectService.calledMethod);
        assertEquals("component:10:member:member_id", componentService.calledMethod);
    }

    private CommandLine commandLine(Object command) {
        CommandLine commandLine = new CommandLine(command);
        commandLine.setExecutionExceptionHandler(new CommandExceptionHandler());
        return commandLine;
    }

    private PromptHelper prompt(String input) {
        return new PromptHelper(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    private static CommitLogView commitView() {
        CommitChangeDetailView change = new CommitChangeDetailView();
        change.setComponentType("COLUMN");
        change.setComponentName("member_id");
        change.setComponentComment("회원 ID");
        change.setChangeType("MOD");
        change.setChangeColumn("DATA_TYPE");
        change.setBeforeValue("integer");
        change.setAfterValue("bigint");

        CommitTargetView target = new CommitTargetView();
        target.setCommitTargetId(1L);
        target.setObjectType("TABLE");
        target.setObjectName("member");
        target.setObjectComment("회원");
        target.setChangeType("MOD");
        target.setChanges(List.of(change));

        CommitLogView view = new CommitLogView();
        view.setCommitLogId(10L);
        view.setDbAlias("local");
        view.setDbName("testdb");
        view.setCommitTitle("테스트 커밋");
        view.setCommitContent("내용");
        view.setUserId(1L);
        view.setCommitCreatedAt("2026-01-01 00:00:00");
        view.setTargets(List.of(target));
        return view;
    }

    private static class StubCommitService extends CommitService {
        private String commitTitle;
        private String commitContent;
        private RuntimeException exception;
        private int limit;
        private List<CommitLogView> commitLogs = List.of();
        private CommitLogView view;
        private String calledMethod;

        @Override
        public void commit(String title, String content) {
            if (exception != null) {
                throw exception;
            }
            commitTitle = title;
            commitContent = content;
        }

        @Override
        public List<CommitLogView> commitLogList(int limit) {
            this.limit = limit;
            return commitLogs;
        }

        @Override
        public CommitLogView commitTargetList(Long commitLogId) {
            calledMethod = "targetList:" + commitLogId;
            return view;
        }

        @Override
        public CommitLogView commitObjectDetail(Long commitLogId, String objectName) {
            calledMethod = "object:" + commitLogId + ":" + objectName;
            return view;
        }

        @Override
        public CommitLogView commitComponentDetail(Long commitLogId, String objectName, String componentName) {
            calledMethod = "component:" + commitLogId + ":" + objectName + ":" + componentName;
            return view;
        }
    }
}
