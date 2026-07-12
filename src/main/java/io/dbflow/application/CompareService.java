package io.dbflow.application;

import io.dbflow.common.enums.ChangeColumn;
import io.dbflow.common.enums.ChangeType;
import io.dbflow.common.enums.ComponentType;
import io.dbflow.common.enums.ObjectType;
import io.dbflow.domain.ColumnSnapshot;
import io.dbflow.domain.Snapshot;
import io.dbflow.domain.TableSnapshot;
import io.dbflow.domain.WorkChange;
import io.dbflow.domain.WorkComponent;
import io.dbflow.domain.WorkDiffResult;
import io.dbflow.domain.WorkTarget;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CompareService {

    private final SnapshotService snapshotService;

    public CompareService() {
        this(new SnapshotService());
    }

    public CompareService(SnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    public WorkDiffResult compare(Long dbConfigId) {
        WorkDiffResult result = new WorkDiffResult();
        Snapshot collectSnapshot = snapshotService.findCollectSnapshot(dbConfigId);
        Snapshot currentSnapshot = snapshotService.findCurrentSnapshot(dbConfigId);

        compareTables(dbConfigId, collectSnapshot, currentSnapshot, result);
        compareColumns(collectSnapshot, currentSnapshot, result);

        return result;
    }

    private void compareTables(Long dbConfigId, Snapshot collectSnapshot, Snapshot currentSnapshot, WorkDiffResult result) {
        Map<String, TableSnapshot> collectMap = createTableMap(collectSnapshot);
        Map<String, TableSnapshot> currentMap = createTableMap(currentSnapshot);

        for (String tableName : collectMap.keySet()) {
            TableSnapshot collect = collectMap.get(tableName);
            TableSnapshot current = currentMap.get(tableName);

            if (current == null) {
                result.addTarget(new WorkTarget(dbConfigId, ObjectType.TABLE.name(), collect.getTableName(), collect.getTableComment(), ChangeType.ADD.name()));
                continue;
            }

            WorkTarget target = null;

            if (!Objects.equals(collect.getTableComment(), current.getTableComment())) {
                target = new WorkTarget(dbConfigId, ObjectType.TABLE.name(), collect.getTableName(), collect.getTableComment(), ChangeType.MOD.name());
                WorkComponent component = target.addComponent(new WorkComponent(ComponentType.TABLE.name(), collect.getTableName(), collect.getTableComment(), ChangeType.MOD.name()));
                component.addChange(new WorkChange(ChangeColumn.TABLE_COMMENT.name(), current.getTableComment(), collect.getTableComment()));
            }

            if (target != null) {
                result.addTarget(target);
            }
        }

        for (String tableName : currentMap.keySet()) {
            if (!collectMap.containsKey(tableName)) {
                TableSnapshot current = currentMap.get(tableName);
                result.addTarget(new WorkTarget(dbConfigId, ObjectType.TABLE.name(), current.getTableName(), current.getTableComment(), ChangeType.DEL.name()));
            }
        }
    }

    private void compareColumns(Snapshot collectSnapshot, Snapshot currentSnapshot, WorkDiffResult result) {
        Map<String, WorkTarget> targetMap = result.getTargets()
                .stream()
                .collect(Collectors.toMap(WorkTarget::getObjectName, target -> target));

        Map<String, TableSnapshot> collectTableMap = createTableMap(collectSnapshot);
        Map<String, TableSnapshot> currentTableMap = createTableMap(currentSnapshot);

        for (String tableName : collectTableMap.keySet()) {
            WorkTarget target = targetMap.get(tableName);

            if (target != null && !ChangeType.MOD.name().equals(target.getChangeType())) {
                continue;
            }

            TableSnapshot currentTable = currentTableMap.get(tableName);
            if (currentTable == null) {
                continue;
            }

            compareTableColumns(collectTableMap.get(tableName), currentTable, result, targetMap);
        }
    }

    private void compareTableColumns(
            TableSnapshot collectTable,
            TableSnapshot currentTable,
            WorkDiffResult result,
            Map<String, WorkTarget> targetMap
    ) {
        Map<String, ColumnSnapshot> collectColumnMap = createColumnMap(collectTable);
        Map<String, ColumnSnapshot> currentColumnMap = createColumnMap(currentTable);

        for (String columnName : collectColumnMap.keySet()) {
            ColumnSnapshot collect = collectColumnMap.get(columnName);
            ColumnSnapshot current = currentColumnMap.get(columnName);
            WorkTarget target = targetMap.get(collectTable.getTableName());

            if (current == null) {
                target = getOrCreateModTarget(result, targetMap, collectTable);
                target.addComponent(new WorkComponent(ComponentType.COLUMN.name(), collect.getColumnName(), collect.getColumnComment(), ChangeType.ADD.name()));
                continue;
            }

            WorkComponent component = compareColumnProperties(collect, current);

            if (component != null) {
                target = getOrCreateModTarget(result, targetMap, collectTable);
                target.addComponent(component);
            }
        }

        for (String columnName : currentColumnMap.keySet()) {
            if (!collectColumnMap.containsKey(columnName)) {
                ColumnSnapshot current = currentColumnMap.get(columnName);
                WorkTarget target = getOrCreateModTarget(result, targetMap, collectTable);
                target.addComponent(new WorkComponent(ComponentType.COLUMN.name(), current.getColumnName(), current.getColumnComment(), ChangeType.DEL.name()));
            }
        }
    }

    private WorkComponent compareColumnProperties(ColumnSnapshot collect, ColumnSnapshot current) {
        WorkComponent component = new WorkComponent(ComponentType.COLUMN.name(), collect.getColumnName(), collect.getColumnComment(), ChangeType.MOD.name());

        addChangeIfDifferent(component, ChangeColumn.COLUMN_ORDER, current.getColumnOrder(), collect.getColumnOrder());
        addChangeIfDifferent(component, ChangeColumn.DATA_TYPE, current.getDataType(), collect.getDataType());
        addChangeIfDifferent(component, ChangeColumn.DATA_LENGTH, current.getDataLength(), collect.getDataLength());
        addChangeIfDifferent(component, ChangeColumn.DATA_SCALE, current.getDataScale(), collect.getDataScale());
        addChangeIfDifferent(component, ChangeColumn.NULLABLE_YN, current.getNullableYn(), collect.getNullableYn());
        addChangeIfDifferent(component, ChangeColumn.DEFAULT_VALUE, current.getDefaultValue(), collect.getDefaultValue());
        addChangeIfDifferent(component, ChangeColumn.IDENTITY_YN, current.getIdentityYn(), collect.getIdentityYn());
        addChangeIfDifferent(component, ChangeColumn.IDENTITY_TYPE, current.getIdentityType(), collect.getIdentityType());
        addChangeIfDifferent(component, ChangeColumn.COLUMN_COMMENT, current.getColumnComment(), collect.getColumnComment());

        if (component.getChanges().isEmpty()) {
            return null;
        }

        return component;
    }

    private void addChangeIfDifferent(WorkComponent component, ChangeColumn changeColumn, Object beforeValue, Object afterValue) {
        if (!Objects.equals(beforeValue, afterValue)) {
            component.addChange(new WorkChange(changeColumn.name(), String.valueOf(beforeValue), String.valueOf(afterValue)));
        }
    }

    private WorkTarget getOrCreateModTarget(WorkDiffResult result, Map<String, WorkTarget> targetMap, TableSnapshot table) {
        WorkTarget target = targetMap.get(table.getTableName());

        if (target != null) {
            return target;
        }

        target = new WorkTarget(null, ObjectType.TABLE.name(), table.getTableName(), table.getTableComment(), ChangeType.MOD.name());
        result.addTarget(target);
        targetMap.put(table.getTableName(), target);

        return target;
    }

    private Map<String, TableSnapshot> createTableMap(Snapshot snapshot) {
        Map<String, TableSnapshot> tableMap = new LinkedHashMap<>();
        for (TableSnapshot table : snapshot.getTables()) {
            TableSnapshot duplicate = tableMap.put(table.getTableName(), table);
            if (duplicate != null) {
                throw new IllegalStateException("중복된 테이블 이름이 있습니다. tableName=" + table.getTableName());
            }
        }
        return tableMap;
    }

    private Map<String, ColumnSnapshot> createColumnMap(TableSnapshot table) {
        Map<String, ColumnSnapshot> columnMap = new LinkedHashMap<>();
        for (ColumnSnapshot column : table.getColumns()) {
            ColumnSnapshot duplicate = columnMap.put(column.getColumnName(), column);
            if (duplicate != null) {
                throw new IllegalStateException("중복된 컬럼 이름이 있습니다. tableName=" + table.getTableName() + ", columnName=" + column.getColumnName());
            }
        }
        return columnMap;
    }
}
