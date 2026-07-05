package io.dbflow.infrastructure.repository.mapper;

import io.dbflow.domain.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SnapshotMapper {
    void insertTableSnapshotList(@Param("dbConfigId") Long DbConfigId, @Param("list") List<TableMetadata> tableMetadata);
    void insertColumnSnapshotList(@Param("list") List<CollectColumnSnapshot> columnSnapshotList);

    List<CollectTableSnapshot> selectCollectTableSnapshot(@Param("dbConfigId") Long dbConfigId);
    List<CollectColumnSnapshot> selectCollectColumnSnapshot(@Param("dbConfigId") Long dbConfigId);

    void deleteCollectTableSnapshot(@Param("dbConfigId") Long dbConfigId);
    void deleteCollectColumnSnapshot(@Param("dbConfigId") Long dbConfigId);

    List<CurrentTableSnapshot> selectCurrentTableSnapshot(@Param("dbConfigId") Long dbConfigId);
    List<CurrentColumnSnapshot> selectCurrentColumnSnapshot(@Param("dbConfigId") Long dbConfigId);

    void deleteCommitCurrentTableSnapshot(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);
    void deleteCommitCurrentColumnSnapshot(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);

    void insertCommitCurrentTableSnapshot(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);
    void insertCommitCurrentColumnSnapshot(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);

    void insertCommitHistoryTableSnapshot(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);
    void insertCommitHistoryColumnSnapshot(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);

    void insertCommitDeleteHistoryTableSnapshot(@Param("commitLogId") Long commitLogId);
    void insertCommitDeleteHistoryColumnSnapshot(@Param("commitLogId") Long commitLogId);
}
