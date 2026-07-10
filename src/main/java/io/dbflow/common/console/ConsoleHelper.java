package io.dbflow.common.console;

import io.dbflow.dto.CommitComponentChangeView;
import io.dbflow.dto.CommitLogView;
import io.dbflow.dto.CommitTargetView;

import java.util.List;

public class ConsoleHelper {

    public static void success(String message) {
        System.out.println();
        System.out.println("✅ " + message);
    }

    public static void error(String message) {
        System.out.println();
        System.err.println("❌ " + message);
    }

    public static void info(String message) {
        System.out.println("ℹ️ " + message);
        System.out.println();
    }

    public static void commitLogInfo(CommitLogView commitLogView) {
        System.out.println("Commit");
        System.out.println("----------------------------------------");
        System.out.println("DB Alias : " + commitLogView.getDbAlias());
        System.out.println("DB Name  : " + commitLogView.getDbName());
        System.out.println("Title    : " + commitLogView.getCommitTitle());
        System.out.println("Content  : " + commitLogView.getCommitContent());
        System.out.println("UserId   : " + commitLogView.getUserId());
        System.out.println("CreateAt : " + commitLogView.getCommitCreatedAt());
        System.out.println();
        System.out.println("Commit Objects");
    }

    public static void commitTargetInfo(CommitLogView commitLogView) {
        CommitTargetView target = commitLogView.getCommitTargetViewList().get(0);
        List<CommitComponentChangeView> commitComponentChangeViewList = target.getCommitComponentChangeViews();

        System.out.println("Commit");
        System.out.println("----------------------------------------");
        System.out.println("DB Alias           : " + commitLogView.getDbAlias());
        System.out.println("DB Name            : " + commitLogView.getDbName());
        System.out.println("Title              : " + commitLogView.getCommitTitle());
        System.out.println("Object Name        : " + target.getObjectName());
        System.out.println("Object Comment     : " + target.getObjectComment());
        System.out.println("Object Change Type : " + target.getChangeType());
        System.out.println("CreateAt           : " + commitLogView.getCommitCreatedAt());
        System.out.println("----------------------------------------");

        if(!commitComponentChangeViewList.isEmpty()) {
            System.out.println();
            System.out.println("Commit Components, Changes");

            TablePrinter.printCommitComponentChangeList(commitComponentChangeViewList);
        }
    }


    public static void commitComponentInfo(CommitLogView commitLogView) {
        CommitTargetView target = commitLogView.getCommitTargetViewList().get(0);
        CommitComponentChangeView component = target.getCommitComponentChangeViews().get(0);
        List<CommitComponentChangeView> changeList = target.getCommitComponentChangeViews();

        System.out.println("Commit");
        System.out.println("----------------------------------------");
        System.out.println("DB Alias              : " + commitLogView.getDbAlias());
        System.out.println("DB Name               : " + commitLogView.getDbName());
        System.out.println("Title                 : " + commitLogView.getCommitTitle());
        System.out.println("Object Name           : " + target.getObjectName());
        System.out.println("Object Change Type    : " + target.getChangeType());
        System.out.println("Component Name        : " + component.getComponentName());
        System.out.println("Component Comment     : " + component.getComponentComment());
        System.out.println("Component Change Type : " + component.getChangeType());
        System.out.println("CreateAt              : " + commitLogView.getCommitCreatedAt());
        System.out.println("----------------------------------------");

        if(!changeList.isEmpty()) {
            System.out.println();
            System.out.println("Commit Changes");

            TablePrinter.printCommitChangeList(changeList);
        }
    }
}
