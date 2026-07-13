package io.dbflow.infrastructure.repository.mapper;

import io.dbflow.domain.ColumnSnapshot;
import io.dbflow.domain.TableMetadata;
import io.dbflow.domain.TableSnapshot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SnapshotMapper {
    void insertTableSnapshotList(@Param("dbConfigId") Long DbConfigId, @Param("list") List<TableMetadata> tableMetadata);
    void insertColumnSnapshotList(@Param("list") List<ColumnSnapshot> columnSnapshotList);

    List<TableSnapshot> selectCollectTableSnapshot(@Param("dbConfigId") Long dbConfigId);
    List<ColumnSnapshot> selectCollectColumnSnapshot(@Param("dbConfigId") Long dbConfigId);

    void deleteCollectTableSnapshot(@Param("dbConfigId") Long dbConfigId);
    void deleteCollectColumnSnapshot(@Param("dbConfigId") Long dbConfigId);

    List<TableSnapshot> selectCurrentTableSnapshot(@Param("dbConfigId") Long dbConfigId);
    List<ColumnSnapshot> selectCurrentColumnSnapshot(@Param("dbConfigId") Long dbConfigId);

    void deleteCommitCurrentTableSnapshot(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);
    void deleteCommitCurrentColumnSnapshot(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);

    void insertCommitCurrentTableSnapshot(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);
    void insertCommitCurrentColumnSnapshot(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);

    void insertCommitHistoryTableSnapshot(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);
    void insertCommitHistoryColumnSnapshot(@Param("commitLogId") Long commitLogId, @Param("dbConfigId") Long dbConfigId);

    void insertCommitDeleteHistoryTableSnapshot(@Param("commitLogId") Long commitLogId);
    void insertCommitDeleteHistoryColumnSnapshot(@Param("commitLogId") Long commitLogId);
}
