package io.dbflow.common.console;

import com.github.freva.asciitable.AsciiTable;
import io.dbflow.domain.DbConfig;
import io.dbflow.dto.CommitComponentChangeView;
import io.dbflow.dto.CommitLogView;
import io.dbflow.dto.CommitTargetView;

import java.util.List;

public class TablePrinter {

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
                "No", "Chang Type", "Object Type", "Object Name"
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

    public static void printCommitComponentChangeList(List<CommitComponentChangeView> commitComponentChangeViews) {
        String[] headers = {
                "No", "Chang Type", "Component Type", "Component Name", "Change Column", "Before Value", "After Value"
        };

        String[][] data = new String[commitComponentChangeViews.size()][headers.length];

        for (int i = 0; i < commitComponentChangeViews.size(); i++) {
            CommitComponentChangeView c = commitComponentChangeViews.get(i);

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

    public static void printCommitChangeList(List<CommitComponentChangeView> commitComponentChangeViews) {
        String[] headers = {
                "No", "Change Column", "Before Value", "After Value"
        };

        String[][] data = new String[commitComponentChangeViews.size()][headers.length];

        for (int i = 0; i < commitComponentChangeViews.size(); i++) {
            CommitComponentChangeView c = commitComponentChangeViews.get(i);

            data[i] = new String[] {
                    String.valueOf(i + 1),
                    c.getChangeColumn(),
                    c.getBeforeValue(),
                    c.getAfterValue()
            };
        }

        System.out.println(AsciiTable.getTable(headers, data));
    }
}
