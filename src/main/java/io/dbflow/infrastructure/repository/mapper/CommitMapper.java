package io.dbflow.infrastructure.repository.mapper;

import io.dbflow.domain.CommitLog;
import org.apache.ibatis.annotations.Param;

public interface CommitMapper {

    void insertCommitLog(CommitLog commitLog);

    void insertCommitTargetFromWork(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);

    void insertCommitComponentFromWork(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);

    void insertCommitChangeFromWork(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);

    void deleteWorkChange(@Param("dbConfigId") Long dbConfigId);

    void deleteWorkComponent(@Param("dbConfigId") Long dbConfigId);

    void deleteWorkTarget(@Param("dbConfigId") Long dbConfigId);
}
