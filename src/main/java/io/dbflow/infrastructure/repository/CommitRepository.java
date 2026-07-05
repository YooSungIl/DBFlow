package io.dbflow.infrastructure.repository;

import io.dbflow.common.Exception.RepositoryException;
import io.dbflow.domain.CommitLog;
import io.dbflow.infrastructure.repository.mapper.CommitMapper;
import org.apache.ibatis.session.SqlSession;

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
            throw new RepositoryException("커밋 저장 중 오류가 발생했습니다.", e);
        }
    }
}
