package io.dbflow.infrastructure.repository.mapper;

import io.dbflow.domain.CommitLog;
import io.dbflow.domain.User;
import io.dbflow.dto.CommitComponentChangeView;
import io.dbflow.dto.CommitLogView;
import io.dbflow.dto.CommitTargetView;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommitMapper {

    void insertCommitLog(CommitLog commitLog);

    void insertCommitTargetFromWork(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);

    void insertCommitComponentFromWork(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);

    void insertCommitChangeFromWork(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);

    List<CommitLogView> selectCommitLogList(@Param("user") User user, @Param("limit") int limit);

    CommitLogView selectCommitLog(@Param("user") User user, @Param("commitLogId") Long commitLogId);

    List<CommitTargetView> selectCommitTargetList(@Param("commitLogId") Long commitLogId);

    CommitTargetView selectCommitTarget(@Param("commitLogId") Long commitLogId, @Param("objectName") String objectName);

    List<CommitComponentChangeView> selectCommitComponentChangeList(@Param("commitTargetId") Long commitTargetId, @Param("componentName") String componentName);

}
