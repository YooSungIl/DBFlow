package io.dbflow.domain;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SnapshotBundle {

    private final Map<String, CollectTableSnapshot> collectTableMap;
    private final Map<String, CurrentTableSnapshot> currentTableMap;

    private final Map<String, Map<String, CollectColumnSnapshot>> collectColumnMap;
    private final Map<String, Map<String, CurrentColumnSnapshot>> currentColumnMap;

    public SnapshotBundle(
            List<CollectTableSnapshot> collectTables,
            List<CurrentTableSnapshot> currentTables,
            List<CollectColumnSnapshot> collectColumns,
            List<CurrentColumnSnapshot> currentColumns
    ) {
        this.collectTableMap = collectTables.stream()
                .collect(Collectors.toMap(
                        CollectTableSnapshot::getTableName,
                        table -> table
                ));

        this.currentTableMap = currentTables.stream()
                .collect(Collectors.toMap(
                        CurrentTableSnapshot::getTableName,
                        table -> table
                ));

        this.collectColumnMap = collectColumns.stream()
                .collect(Collectors.groupingBy(
                        CollectColumnSnapshot::getTableName,
                        Collectors.toMap(
                                CollectColumnSnapshot::getColumnName,
                                column -> column
                        )
                ));

        this.currentColumnMap = currentColumns.stream()
                .collect(Collectors.groupingBy(
                        CurrentColumnSnapshot::getTableName,
                        Collectors.toMap(
                                CurrentColumnSnapshot::getColumnName,
                                column -> column
                        )
                ));
    }

    public Map<String, CollectTableSnapshot> getCollectTableMap() {
        return collectTableMap;
    }

    public Map<String, CurrentTableSnapshot> getCurrentTableMap() {
        return currentTableMap;
    }

    public Map<String, CollectColumnSnapshot> getCollectColumnMap(String tableName) {
        return collectColumnMap.getOrDefault(tableName, Map.of());
    }

    public Map<String, CurrentColumnSnapshot> getCurrentColumnMap(String tableName) {
        return currentColumnMap.getOrDefault(tableName, Map.of());
    }
}
