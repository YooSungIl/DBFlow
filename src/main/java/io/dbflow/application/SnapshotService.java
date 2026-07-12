package io.dbflow.application;

import io.dbflow.domain.*;
import io.dbflow.common.enums.SnapshotType;
import io.dbflow.infrastructure.repository.SnapshotRepository;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class SnapshotService {

    private final SnapshotRepository snapshotRepository;

    public SnapshotService() {
        this(new SnapshotRepository());
    }

    public SnapshotService(SnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    public void insertTableSnapshotList(Long dbConfigId, List<TableMetadata> tableMetadataList) {
        snapshotRepository.insertTableSnapshotList(dbConfigId, tableMetadataList);
    }

    public void insertTableSnapshotList(Long dbConfigId, List<TableMetadata> tableMetadataList, SqlSession session) {
        snapshotRepository.insertTableSnapshotList(dbConfigId, tableMetadataList, session);
    }

    public void insertColumnSnapshotList(List<ColumnSnapshot> columnSnapshotList) {
        snapshotRepository.insertColumnSnapshotList(columnSnapshotList);
    }

    public void insertColumnSnapshotList(List<ColumnSnapshot> columnSnapshotList, SqlSession session) {
        snapshotRepository.insertColumnSnapshotList(columnSnapshotList, session);
    }

    public List<TableSnapshot> selectCollectTableSnapshot(Long dbConfigId) {
        return snapshotRepository.selectCollectTableSnapshot(dbConfigId);
    }

    public List<TableSnapshot> selectCollectTableSnapshot(Long dbConfigId, SqlSession session) {
        return snapshotRepository.selectCollectTableSnapshot(dbConfigId, session);
    }

    public List<ColumnSnapshot> selectCollectColumnSnapshot(Long dbConfigId) {
        return snapshotRepository.selectCollectColumnSnapshot(dbConfigId);
    }

    public void deleteCollectedSnapshot(Long dbConfigId) {
        snapshotRepository.deleteCollectedSnapshot(dbConfigId);
    }

    public void deleteCollectedSnapshot(Long dbConfigId, SqlSession session) {
        snapshotRepository.deleteCollectedSnapshot(dbConfigId, session);
    }

    public List<TableSnapshot> selectCurrentTableSnapshot(Long dbConfigId) {
        return snapshotRepository.selectCurrentTableSnapshot(dbConfigId);
    }

    public List<ColumnSnapshot> selectCurrentColumnSnapshot(Long dbConfigId) {
        return snapshotRepository.selectCurrentColumnSnapshot(dbConfigId);
    }

    public Snapshot findCollectSnapshot(Long dbConfigId) {
        List<TableSnapshot> tables = snapshotRepository.selectCollectTableSnapshot(dbConfigId);
        List<ColumnSnapshot> columns = snapshotRepository.selectCollectColumnSnapshot(dbConfigId);
        return new Snapshot(SnapshotType.COLLECT, dbConfigId, null, tables, columns);
    }

    public Snapshot findCurrentSnapshot(Long dbConfigId) {
        List<TableSnapshot> tables = snapshotRepository.selectCurrentTableSnapshot(dbConfigId);
        List<ColumnSnapshot> columns = snapshotRepository.selectCurrentColumnSnapshot(dbConfigId);
        return new Snapshot(SnapshotType.CURRENT, dbConfigId, null, tables, columns);
    }
}
