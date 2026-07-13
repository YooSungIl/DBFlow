package io.dbflow.application;

import io.dbflow.domain.ColumnMetadata;
import io.dbflow.domain.ColumnSnapshot;
import io.dbflow.domain.DbConfig;
import io.dbflow.domain.TableMetadata;
import io.dbflow.domain.TableSnapshot;
import io.dbflow.infrastructure.external.repository.MetadataCollector;
import io.dbflow.infrastructure.mybatis.MainMyBatisSqlSessionFactory;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetadataCollectService {
    private final MetadataCollector metadataCollector;
    private final SnapshotService snapshotService;

    public MetadataCollectService(MetadataCollector metadataCollector) {
        this(metadataCollector, new SnapshotService());
    }

    public MetadataCollectService(MetadataCollector metadataCollector, SnapshotService snapshotService) {
        this.metadataCollector = metadataCollector;
        this.snapshotService = snapshotService;
    }

    public void collect(DbConfig dbConfig) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                snapshotService.deleteCollectedSnapshot(dbConfig.getDbConfigId(), session);

                collectTables(dbConfig, session);
                collectColumns(dbConfig, session);

                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw e;
            }
        }
    }

    public void collectTables(DbConfig dbConfig) {
        List<TableMetadata> tableMetadataList = metadataCollector.collectTableSnapshotList(dbConfig);
        snapshotService.insertTableSnapshotList(dbConfig.getDbConfigId(), tableMetadataList);
    }

    public void collectTables(DbConfig dbConfig, SqlSession session) {
        List<TableMetadata> tableMetadataList = metadataCollector.collectTableSnapshotList(dbConfig);
        snapshotService.insertTableSnapshotList(dbConfig.getDbConfigId(), tableMetadataList, session);
    }

    public void collectColumns(DbConfig dbConfig) {
        // 1. 이미 저장된 테이블 스냅샷 조회
        List<TableSnapshot> tableSnapshotList = snapshotService.selectCollectTableSnapshot(dbConfig.getDbConfigId());

        // 5. SQLite에 컬럼 스냅샷 저장
        snapshotService.insertColumnSnapshotList(createColumnSnapshotList(dbConfig, tableSnapshotList));
    }

    public void collectColumns(DbConfig dbConfig, SqlSession session) {
        // 1. 이미 저장된 테이블 스냅샷 조회
        List<TableSnapshot> tableSnapshotList = snapshotService.selectCollectTableSnapshot(dbConfig.getDbConfigId(), session);

        // 5. SQLite에 컬럼 스냅샷 저장
        snapshotService.insertColumnSnapshotList(createColumnSnapshotList(dbConfig, tableSnapshotList), session);
    }

    private List<ColumnSnapshot> createColumnSnapshotList(DbConfig dbConfig, List<TableSnapshot> tableSnapshotList) {
        // 2. 외부 DB에서 컬럼 메타정보 조회
        List<ColumnMetadata> columnMetadataList = metadataCollector.collectColumnSnapshotList(dbConfig, tableSnapshotList);

        // 3. tableName -> collectTableId Map 생성
        Map<String, Long> tableIdMap = tableSnapshotList.stream()
                .collect(Collectors.toMap(
                        TableSnapshot::getTableName,
                        TableSnapshot::getTableSnapshotId
                ));

        // 4. 컬럼 메타정보를 컬럼 스냅샷으로 변환
        List<ColumnSnapshot> columnSnapshotList = new ArrayList<>();

        for (ColumnMetadata columnMetadata : columnMetadataList) {
            Long collectTableId = tableIdMap.get(columnMetadata.getTableName());

            if (collectTableId == null) {
                throw new IllegalStateException("컬럼에 매칭되는 테이블 스냅샷이 없습니다. tableName=" + columnMetadata.getTableName() + ", columnName=" + columnMetadata.getColumnName());
            }

            ColumnSnapshot columnSnapshot = new ColumnSnapshot();

            columnSnapshot.setTableSnapshotId(collectTableId);
            columnSnapshot.setColumnName(columnMetadata.getColumnName());
            columnSnapshot.setColumnComment(columnMetadata.getColumnComment());
            columnSnapshot.setColumnOrder(columnMetadata.getColumnOrder());
            columnSnapshot.setDataType(columnMetadata.getDataType());
            columnSnapshot.setDataLength(columnMetadata.getDataLength());
            columnSnapshot.setDataScale(columnMetadata.getDataScale());
            columnSnapshot.setNullableYn(columnMetadata.getNullableYn());
            columnSnapshot.setDefaultValue(columnMetadata.getDefaultValue());
            columnSnapshot.setIdentityYn(columnMetadata.getIdentityYn());
            columnSnapshot.setIdentityType(columnMetadata.getIdentityType());

            columnSnapshotList.add(columnSnapshot);
        }

        return columnSnapshotList;
    }

}
