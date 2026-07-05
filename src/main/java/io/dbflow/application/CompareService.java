package io.dbflow.application;

import io.dbflow.domain.*;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CompareService {

    public WorkDiffResult compare(Long dbConfigId) {
        WorkDiffResult result = new WorkDiffResult();

        SnapshotService snapshotService = new SnapshotService();
        SnapshotBundle snapshot = snapshotService.findSnapshotBundle(dbConfigId);

        compareTables(dbConfigId, snapshot, result);
        compareColumns(snapshot, result);

        return result;
    }

    private void compareTables(Long dbConfigId, SnapshotBundle snapshot, WorkDiffResult result) {
        Map<String, CollectTableSnapshot> collectMap = snapshot.getCollectTableMap();
        Map<String, CurrentTableSnapshot> currentMap = snapshot.getCurrentTableMap();

        // 수집 정보 대상으로 현재 정보와 비교(신규, 수정)
        for (String tableName : collectMap.keySet()) {
            //테이블 정보 수집
            CollectTableSnapshot collect = collectMap.get(tableName);
            CurrentTableSnapshot current = currentMap.get(tableName);

            //현재 테이블에 데이터가 없을 경우 신규 등록
            if (current == null) {
                result.addTarget(new WorkTarget(dbConfigId, "TABLE", collect.getTableName(), collect.getTableComment(), "ADD"));
                continue;
            }

            //현재 테이블이 있지만 테이블 설명이 다를 경우 수정
            WorkTarget target = null;

            if (!Objects.equals(collect.getTableComment(), current.getTableComment())) {
                target = new WorkTarget(dbConfigId, "TABLE", collect.getTableName(), collect.getTableComment(), "MOD");
                WorkComponent component = target.addComponent(new WorkComponent("TABLE", collect.getTableName(), collect.getTableComment(), "MOD"));
                component.addChange(new WorkChange("TABLE_COMMENT", current.getTableComment(), collect.getTableComment()));
            }

            if (target != null) {
                result.addTarget(target);
            }
        }

        //현재 정보 대상으로 수집 정보와 비교(삭제)
        for (String tableName : currentMap.keySet()) {
            if (!collectMap.containsKey(tableName)) {
                CurrentTableSnapshot current = currentMap.get(tableName);
                result.addTarget(new WorkTarget(dbConfigId, "TABLE", current.getTableName(), current.getTableComment(), "DEL"));
            }
        }
    }

    private void compareColumns(SnapshotBundle snapshot, WorkDiffResult result) {
        //위에서 작업한 대상 목록을 맵으로 변환
        Map<String, WorkTarget> targetMap = result.getTargets()
                .stream()
                .collect(Collectors.toMap(WorkTarget::getObjectName, target -> target));

        //수집, 현재 작업 정보
        Map<String, CollectTableSnapshot> collectTableMap = snapshot.getCollectTableMap();
        Map<String, CurrentTableSnapshot> currentTableMap = snapshot.getCurrentTableMap();

        //수집한 테이블을 기준으로 반복문
        for (String tableName : collectTableMap.keySet()) {
            WorkTarget target = targetMap.get(tableName);

            // 테이블 ADD / DEL은 컬럼 비교 제외
            if (target != null && !"MOD".equals(target.getChangeType())) {
                continue;
            }

            // 현재에도 있고 수집에도 있는 테이블만 컬럼 비교
            if (!currentTableMap.containsKey(tableName)) {
                continue;
            }

            // 수정시
            compareTableColumns(tableName, snapshot, result, targetMap);
        }
    }

    private void compareTableColumns(String tableName, SnapshotBundle snapshot, WorkDiffResult result, Map<String, WorkTarget> targetMap) {
        Map<String, CollectColumnSnapshot> collectColumnMap = snapshot.getCollectColumnMap(tableName);
        Map<String, CurrentColumnSnapshot> currentColumnMap = snapshot.getCurrentColumnMap(tableName);

        //수집한 정보를 가지고 비교
        for (String columnName : collectColumnMap.keySet()) {
            CollectColumnSnapshot collect = collectColumnMap.get(columnName);
            CurrentColumnSnapshot current = currentColumnMap.get(columnName);

            //수집정보로 현재 작업했던 대상 정보 수집
            WorkTarget target = targetMap.get(tableName);

            //컬럼 신규 등록
            if (current == null) {
                //이 컬럼이 속한 테이블이 없는 경우 workTarget 생성
                target = getOrCreateModTarget(result, targetMap, collect);

                target.addComponent(new WorkComponent("COLUMN", collect.getColumnName(), collect.getColumnComment(), "ADD"));
                continue;
            }

            //컬럼 수정 등록 (컴포넌트를 만들어 한 번에 대상에 저장)
            WorkComponent component = compareColumnProperties(collect, current);

            if (component != null) {
                target = getOrCreateModTarget(result, targetMap, collect);

                target.addComponent(component);
            }
        }

        // 현재 작업을 비교
        for (String columnName : currentColumnMap.keySet()) {
            if (!collectColumnMap.containsKey(columnName)) {
                CurrentColumnSnapshot current = currentColumnMap.get(columnName);
                WorkTarget target = getOrCreateModTarget(result, targetMap, current);

                target.addComponent(new WorkComponent("COLUMN", current.getColumnName(), current.getColumnComment(), "DEL"
                ));
            }
        }
    }

    private WorkComponent compareColumnProperties(CollectColumnSnapshot collect, CurrentColumnSnapshot current) {
        WorkComponent component = new WorkComponent("COLUMN", collect.getColumnName(), collect.getColumnComment(), "MOD");

        addChangeIfDifferent(component, "COLUMN_ORDER", current.getColumnOrder(), collect.getColumnOrder());
        addChangeIfDifferent(component, "DATA_TYPE", current.getDataType(), collect.getDataType());
        addChangeIfDifferent(component, "DATA_LENGTH", current.getDataLength(), collect.getDataLength());
        addChangeIfDifferent(component, "DATA_SCALE", current.getDataScale(), collect.getDataScale());
        addChangeIfDifferent(component, "NULLABLE_YN", current.getNullableYn(), collect.getNullableYn());
        addChangeIfDifferent(component, "DATA_DEFAULT", current.getDefaultValue(), collect.getDefaultValue());
        addChangeIfDifferent(component, "IDENTITY_YN", current.getIdentityYn(), collect.getIdentityYn());
        addChangeIfDifferent(component, "IDENTITY_TYPE", current.getIdentityType(), collect.getIdentityType());
        addChangeIfDifferent(component, "COLUMN_COMMENT", current.getColumnComment(), collect.getColumnComment());

        if (component.getChanges().isEmpty()) {
            return null;
        }

        return component;
    }

    private void addChangeIfDifferent(WorkComponent component, String changeColumn, Object beforeValue, Object afterValue) {
        if (!Objects.equals(beforeValue, afterValue)) {
            component.addChange(new WorkChange(changeColumn, String.valueOf(beforeValue), String.valueOf(afterValue)));
        }
    }

    private WorkTarget getOrCreateModTarget(WorkDiffResult result, Map<String, WorkTarget> targetMap, CollectColumnSnapshot column) {
        return getOrCreateModTarget(result, targetMap, column.getTableName(), column.getTableComment());
    }

    private WorkTarget getOrCreateModTarget(WorkDiffResult result, Map<String, WorkTarget> targetMap, CurrentColumnSnapshot column) {
        return getOrCreateModTarget(result, targetMap, column.getTableName(), column.getTableComment());
    }

    private WorkTarget getOrCreateModTarget(WorkDiffResult result, Map<String, WorkTarget> targetMap, String tableName, String tableComment) {
        WorkTarget target = targetMap.get(tableName);

        if (target != null) {
            return target;
        }

        target = new WorkTarget(null, "TABLE", tableName, tableComment, "MOD");

        result.addTarget(target);
        targetMap.put(tableName, target);

        return target;
    }
}
