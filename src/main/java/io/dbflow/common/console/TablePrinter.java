package io.dbflow.common.console;

import com.github.freva.asciitable.AsciiTable;
import io.dbflow.domain.DbConfig;

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
}
