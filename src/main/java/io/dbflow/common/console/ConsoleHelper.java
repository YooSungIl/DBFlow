package io.dbflow.common.console;

import com.github.freva.asciitable.AsciiTable;
import io.dbflow.domain.DbConfig;
import io.dbflow.domain.User;
import io.dbflow.domain.Work;
import io.dbflow.dto.CommitChangeDetailView;
import io.dbflow.dto.CommitLogView;
import io.dbflow.dto.CommitTargetView;

import java.util.List;

public final class ConsoleHelper {

    private ConsoleHelper() {
    }

    public static void success(String message) {
        System.out.println();
        System.out.println("✅ " + message);
    }

    public static void error(String message) {
        System.err.println();
        System.err.println("❌ " + message);
    }

    public static void info(String message) {
        System.out.println("ℹ️ " + message);
        System.out.println();
    }

    public static void printUser(User user) {
        System.out.println();
        System.out.println("이름 : " + user.getUserName());
        System.out.println("이메일 : " + user.getUserEmail());
    }

    public static void printWork(Work work) {
        System.out.println();
        System.out.println("이름 : " + work.getUserName());
        System.out.println("이메일 : " + work.getUserEmail());
        System.out.println("DB별칭 : " + work.getDbAlias());
        System.out.println("DB종류 : " + work.getDbType());
        System.out.println("DBHost : " + work.getDbHost());
        System.out.println("DBPort : " + work.getDbPort());
        System.out.println("DB명 : " + work.getDbName());
        System.out.println("DBSchema : " + work.getDbSchema());
    }

    public static void printDbConfig(String dbAlias, DbConfig dbConfig) {
        System.out.println();
        System.out.println("[" + dbAlias + "] DB접속 정보");
        System.out.println("Alias: " + dbConfig.getDbAlias());
        System.out.println("DB Type: " + dbConfig.getDbType());
        System.out.println("DB Host: " + dbConfig.getDbHost());
        System.out.println("DB Port: " + dbConfig.getDbPort());
        System.out.println("DB Name: " + dbConfig.getDbName());
        System.out.println("DB Schema: " + dbConfig.getDbSchema());
        System.out.println("DB User: " + dbConfig.getDbUser());
        System.out.println("DB Password: ********");
        System.out.println("DB UseYN: " + dbConfig.getUseYn());
        System.out.println("Created AT: " + dbConfig.getCreatedAt());
        System.out.println("Updated AT: " + dbConfig.getUpdateAt());
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
        CommitTargetView target = firstTarget(commitLogView);
        List<CommitChangeDetailView> changes = target.getChanges();

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

        if (!changes.isEmpty()) {
            System.out.println();
            System.out.println("Commit Components, Changes");

            printCommitComponentChangeList(changes);
        }
    }

    public static void commitComponentInfo(CommitLogView commitLogView) {
        CommitTargetView target = firstTarget(commitLogView);
        CommitChangeDetailView component = firstComponentChange(target);
        List<CommitChangeDetailView> changes = target.getChanges();

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

        if (!changes.isEmpty()) {
            System.out.println();
            System.out.println("Commit Changes");

            printCommitChangeList(changes);
        }
    }

    public static void printDbConfigList(List<DbConfig> configs) {
        String[] headers = {
                "No", "Alias", "Type", "Host", "Port", "Database", "Schema", "Use"
        };

        String[][] data = new String[configs.size()][headers.length];

        for (int i = 0; i < configs.size(); i++) {
            DbConfig c = configs.get(i);

            data[i] = new String[] {
                    String.valueOf(i + 1),
                    c.getDbAlias(),
                    c.getDbType(),
                    c.getDbHost(),
                    String.valueOf(c.getDbPort()),
                    c.getDbName(),
                    c.getDbSchema(),
                    c.getUseYn() == 1 ? "Y" : "N"
            };
        }

        System.out.println(AsciiTable.getTable(headers, data));
    }

    public static void printCommitLogList(List<CommitLogView> commitLogs) {
        String[] headers = {
                "No", "Commit Id", "CreateAt", "Title"
        };

        String[][] data = new String[commitLogs.size()][headers.length];

        for (int i = 0; i < commitLogs.size(); i++) {
            CommitLogView c = commitLogs.get(i);

            data[i] = new String[] {
                    String.valueOf(i + 1),
                    String.valueOf(c.getCommitLogId()),
                    c.getCommitCreatedAt(),
                    c.getCommitTitle()
            };
        }

        System.out.println(AsciiTable.getTable(headers, data));
    }

    public static void printCommitTargetList(List<CommitTargetView> commitTargets) {
        String[] headers = {
                "No", "Change Type", "Object Type", "Object Name"
        };

        String[][] data = new String[commitTargets.size()][headers.length];

        for (int i = 0; i < commitTargets.size(); i++) {
            CommitTargetView t = commitTargets.get(i);

            data[i] = new String[] {
                    String.valueOf(i + 1),
                    t.getChangeType(),
                    t.getObjectType(),
                    t.getObjectName()

            };
        }

        System.out.println(AsciiTable.getTable(headers, data));
    }

    public static void printCommitComponentChangeList(List<CommitChangeDetailView> changes) {
        String[] headers = {
                "No", "Change Type", "Component Type", "Component Name", "Change Column", "Before Value", "After Value"
        };

        String[][] data = new String[changes.size()][headers.length];

        for (int i = 0; i < changes.size(); i++) {
            CommitChangeDetailView c = changes.get(i);

            data[i] = new String[] {
                    String.valueOf(i + 1),
                    c.getChangeType(),
                    c.getComponentType(),
                    c.getComponentName(),
                    c.getChangeColumn(),
                    c.getBeforeValue(),
                    c.getAfterValue()
            };
        }

        System.out.println(AsciiTable.getTable(headers, data));
    }

    public static void printCommitChangeList(List<CommitChangeDetailView> changes) {
        String[] headers = {
                "No", "Change Column", "Before Value", "After Value"
        };

        String[][] data = new String[changes.size()][headers.length];

        for (int i = 0; i < changes.size(); i++) {
            CommitChangeDetailView c = changes.get(i);

            data[i] = new String[] {
                    String.valueOf(i + 1),
                    c.getChangeColumn(),
                    c.getBeforeValue(),
                    c.getAfterValue()
            };
        }

        System.out.println(AsciiTable.getTable(headers, data));
    }

    private static CommitTargetView firstTarget(CommitLogView commitLogView) {
        return commitLogView.getTargets().get(0);
    }

    private static CommitChangeDetailView firstComponentChange(CommitTargetView target) {
        return target.getChanges().get(0);
    }
}
