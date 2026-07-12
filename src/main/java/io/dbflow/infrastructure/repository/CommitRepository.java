package io.dbflow.infrastructure.repository;

import io.dbflow.common.Exception.RepositoryException;
import io.dbflow.domain.CommitLog;
import io.dbflow.domain.User;
import io.dbflow.dto.CommitChangeDetailView;
import io.dbflow.dto.CommitLogView;
import io.dbflow.dto.CommitTargetView;
import io.dbflow.infrastructure.mybatis.MainMyBatisSqlSessionFactory;
import io.dbflow.infrastructure.repository.mapper.CommitMapper;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class CommitRepository {

    public void commitWork(CommitLog commitLog, SqlSession session) {
        try {
            CommitMapper mapper = session.getMapper(CommitMapper.class);
            mapper.insertCommitLog(commitLog);

            Long commitLogId = commitLog.getCommitLogId();
            Long dbConfigId = commitLog.getDbConfigId();

            mapper.insertCommitTargetFromWork(commitLogId, dbConfigId);
            mapper.insertCommitComponentFromWork(commitLogId, dbConfigId);
            mapper.insertCommitChangeFromWork(commitLogId, dbConfigId);

        } catch (Exception e) {
            session.rollback();
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public List<CommitLogView> selectCommitLogList(User user, int limit) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            CommitMapper commitMapper = session.getMapper(CommitMapper.class);
            return commitMapper.selectCommitLogList(user, limit);
        } catch (Exception e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    public CommitLogView selectCommitTargetList(User user, Long commitLogId) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            CommitMapper commitMapper = session.getMapper(CommitMapper.class);

            CommitLogView commitLogView = commitMapper.selectCommitLog(user, commitLogId);
            if (commitLogView == null) {
                throw new RepositoryException(RepositoryException.COMMIT_NOT_FOUND);
            }

            List<CommitTargetView> targets = commitMapper.selectCommitTargetList(commitLogView.getCommitLogId());
            if (targets == null || targets.isEmpty()) {
                throw new RepositoryException(RepositoryException.COMMIT_TARGET_NOT_FOUND);
            }

            commitLogView.setTargets(targets);
            return commitLogView;
        } catch (Exception e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    public CommitLogView selectCommitObjectDetail(User user, Long commitLogId, String objectName) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            CommitMapper commitMapper = session.getMapper(CommitMapper.class);

            CommitLogView commit = commitMapper.selectCommitLog(user, commitLogId);
            if (commit == null) {
                throw new RepositoryException(RepositoryException.COMMIT_NOT_FOUND);
            }

            CommitTargetView target = commitMapper.selectCommitTarget(commitLogId, objectName);

            if (target == null) {
                throw new RepositoryException(RepositoryException.COMMIT_TARGET_NOT_FOUND);
            }

            List<CommitChangeDetailView> changes = commitMapper.selectCommitComponentChangeList(target.getCommitTargetId(), null);
            if (changes == null || changes.isEmpty()) {
                throw new RepositoryException(RepositoryException.COMMIT_COMPONENT_NOT_FOUND);
            }

            target.setChanges(changes);
            commit.setTargets(List.of(target));

            return commit;
        }
    }

    public CommitLogView selectCommitComponentDetail(User user, Long commitLogId, String objectName, String componentName) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            CommitMapper commitMapper = session.getMapper(CommitMapper.class);

            CommitLogView commit = commitMapper.selectCommitLog(user, commitLogId);
            if (commit == null) {
                throw new RepositoryException(RepositoryException.COMMIT_NOT_FOUND);
            }

            CommitTargetView target = commitMapper.selectCommitTarget(commitLogId, objectName);

            if (target == null) {
                throw new RepositoryException(RepositoryException.COMMIT_TARGET_NOT_FOUND);
            }

            List<CommitChangeDetailView> changes = commitMapper.selectCommitComponentChangeList(target.getCommitTargetId(), componentName);
            if (changes == null || changes.isEmpty()) {
                throw new RepositoryException(RepositoryException.COMMIT_COMPONENT_NOT_FOUND);
            }

            target.setChanges(changes);
            commit.setTargets(List.of(target));

            return commit;
        }
    }
}
