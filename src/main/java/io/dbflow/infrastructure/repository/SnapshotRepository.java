package io.dbflow.infrastructure.repository;

import io.dbflow.common.Exception.RepositoryException;
import io.dbflow.domain.*;
import io.dbflow.infrastructure.mybatis.MainMyBatisSqlSessionFactory;
import io.dbflow.infrastructure.repository.mapper.SnapshotMapper;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class SnapshotRepository {

    public void insertTableSnapshotList(Long dbConfigId, List<TableMetadata> tableMetadata) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                insertTableSnapshotList(dbConfigId, tableMetadata, session);
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RepositoryException(e.getMessage());
            }
        }
    }

    public void insertTableSnapshotList(Long dbConfigId, List<TableMetadata> tableMetadata, SqlSession session) {
        SnapshotMapper snapshotMapper = session.getMapper(SnapshotMapper.class);
        snapshotMapper.insertTableSnapshotList(dbConfigId, tableMetadata);
    }

    public void insertColumnSnapshotList(List<CollectColumnSnapshot> collectColumnSnapshotList) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                insertColumnSnapshotList(collectColumnSnapshotList, session);
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RepositoryException(e.getMessage());
            }
        }
    }

    public void insertColumnSnapshotList(List<CollectColumnSnapshot> collectColumnSnapshotList, SqlSession session) {
        SnapshotMapper snapshotMapper = session.getMapper(SnapshotMapper.class);
        snapshotMapper.insertColumnSnapshotList(collectColumnSnapshotList);
    }

    public List<CollectTableSnapshot> selectCollectTableSnapshot(Long dbConfigId) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            return selectCollectTableSnapshot(dbConfigId, session);
        } catch (Exception e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    public List<CollectTableSnapshot> selectCollectTableSnapshot(Long dbConfigId, SqlSession session) {
        SnapshotMapper snapshotMapper = session.getMapper(SnapshotMapper.class);
        return snapshotMapper.selectCollectTableSnapshot(dbConfigId);
    }

    public List<CollectColumnSnapshot> selectCollectColumnSnapshot(Long dbConfigId) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            SnapshotMapper snapshotMapper = session.getMapper(SnapshotMapper.class);
            return snapshotMapper.selectCollectColumnSnapshot(dbConfigId);
        } catch (Exception e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    public void deleteCollectedSnapshot(Long dbConfigId) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                deleteCollectedSnapshot(dbConfigId, session);
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RepositoryException(e.getMessage());
            }
        }
    }

    public void deleteCollectedSnapshot(Long dbConfigId, SqlSession session) {
        SnapshotMapper snapshotMapper = session.getMapper(SnapshotMapper.class);
        snapshotMapper.deleteCollectColumnSnapshot(dbConfigId);
        snapshotMapper.deleteCollectTableSnapshot(dbConfigId);
    }

    public List<CurrentTableSnapshot> selectCurrentTableSnapshot(Long dbConfigId) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            SnapshotMapper snapshotMapper = session.getMapper(SnapshotMapper.class);
            return snapshotMapper.selectCurrentTableSnapshot(dbConfigId);
        } catch (Exception e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    public List<CurrentColumnSnapshot> selectCurrentColumnSnapshot(Long dbConfigId) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            SnapshotMapper snapshotMapper = session.getMapper(SnapshotMapper.class);
            return snapshotMapper.selectCurrentColumnSnapshot(dbConfigId);
        } catch (Exception e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    public void insertCommitCurrentSnapshot(CommitLog commitLog, SqlSession session) {
        SnapshotMapper snapshotMapper = session.getMapper(SnapshotMapper.class);
        snapshotMapper.deleteCommitCurrentColumnSnapshot(commitLog.getCommitLogId(), commitLog.getDbConfigId());
        snapshotMapper.deleteCommitCurrentTableSnapshot(commitLog.getCommitLogId(), commitLog.getDbConfigId());
        snapshotMapper.insertCommitCurrentTableSnapshot(commitLog.getCommitLogId(), commitLog.getDbConfigId());
        snapshotMapper.insertCommitCurrentColumnSnapshot(commitLog.getCommitLogId(), commitLog.getDbConfigId());
    }

    public void insertCommitHistorySnapshot(CommitLog commitLog, SqlSession session) {
        SnapshotMapper snapshotMapper = session.getMapper(SnapshotMapper.class);
        snapshotMapper.insertCommitHistoryTableSnapshot(commitLog.getCommitLogId(), commitLog.getDbConfigId());
        snapshotMapper.insertCommitHistoryColumnSnapshot(commitLog.getCommitLogId(), commitLog.getDbConfigId());
        snapshotMapper.insertCommitDeleteHistoryTableSnapshot(commitLog.getCommitLogId());
        snapshotMapper.insertCommitDeleteHistoryColumnSnapshot(commitLog.getCommitLogId());
    }

}
