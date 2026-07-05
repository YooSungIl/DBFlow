package io.dbflow.application;

import io.dbflow.domain.*;
import io.dbflow.infrastructure.repository.SnapshotRepository;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class SnapshotService {

    private final SnapshotRepository snapshotRepository = new SnapshotRepository();

    public void insertTableSnapshotList(Long dbConfigId, List<TableMetadata> tableMetadataList) {
        snapshotRepository.insertTableSnapshotList(dbConfigId, tableMetadataList);
    }

    public void insertTableSnapshotList(Long dbConfigId, List<TableMetadata> tableMetadataList, SqlSession session) {
        snapshotRepository.insertTableSnapshotList(dbConfigId, tableMetadataList, session);
    }

    public void insertColumnSnapshotList(List<CollectColumnSnapshot> collectColumnSnapshotList) {
        snapshotRepository.insertColumnSnapshotList(collectColumnSnapshotList);
    }

    public void insertColumnSnapshotList(List<CollectColumnSnapshot> collectColumnSnapshotList, SqlSession session) {
        snapshotRepository.insertColumnSnapshotList(collectColumnSnapshotList, session);
    }

    public List<CollectTableSnapshot> selectCollectTableSnapshot(Long dbConfigId) {
        return snapshotRepository.selectCollectTableSnapshot(dbConfigId);
    }

    public List<CollectTableSnapshot> selectCollectTableSnapshot(Long dbConfigId, SqlSession session) {
        return snapshotRepository.selectCollectTableSnapshot(dbConfigId, session);
    }

    public List<CollectColumnSnapshot> selectCollectColumnSnapshot(Long dbConfigId) {
        return snapshotRepository.selectCollectColumnSnapshot(dbConfigId);
    }

    public void deleteCollectedSnapshot(Long dbConfigId) {
        snapshotRepository.deleteCollectedSnapshot(dbConfigId);
    }

    public void deleteCollectedSnapshot(Long dbConfigId, SqlSession session) {
        snapshotRepository.deleteCollectedSnapshot(dbConfigId, session);
    }

    public List<CurrentTableSnapshot> selectCurrentTableSnapshot(Long dbConfigId) {
        return snapshotRepository.selectCurrentTableSnapshot(dbConfigId);
    }

    public List<CurrentColumnSnapshot> selectCurrentColumnSnapshot(Long dbConfigId) {
        return snapshotRepository.selectCurrentColumnSnapshot(dbConfigId);
    }

    public SnapshotBundle findSnapshotBundle(Long dbConfigId) {
        List<CollectTableSnapshot> collectTables = snapshotRepository.selectCollectTableSnapshot(dbConfigId);
        List<CurrentTableSnapshot> currentTables = snapshotRepository.selectCurrentTableSnapshot(dbConfigId);
        List<CollectColumnSnapshot> collectColumns = snapshotRepository.selectCollectColumnSnapshot(dbConfigId);
        List<CurrentColumnSnapshot> currentColumns = snapshotRepository.selectCurrentColumnSnapshot(dbConfigId);

        return new SnapshotBundle(collectTables, currentTables, collectColumns, currentColumns);
    }
}
