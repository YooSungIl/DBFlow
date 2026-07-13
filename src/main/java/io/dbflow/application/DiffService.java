package io.dbflow.application;

import io.dbflow.common.enums.DbType;
import io.dbflow.domain.DbConfig;
import io.dbflow.domain.Work;
import io.dbflow.domain.WorkDiffResult;
import io.dbflow.domain.WorkTarget;
import io.dbflow.infrastructure.external.repository.MetadataCollector;
import io.dbflow.infrastructure.external.repository.postgres.PostgresMetadataCollector;
import io.dbflow.infrastructure.repository.DbConfigRepository;

import java.util.List;

public class DiffService {

    private final WorkService workService;
    private final ConnectService connectService;
    private final CompareService compareService;
    private final SnapshotService snapshotService;

    public DiffService() {
        this(
                new WorkService(),
                new ConnectService(new DbConfigRepository()),
                new CompareService(),
                new SnapshotService()
        );
    }

    public DiffService(
            WorkService workService,
            ConnectService connectService,
            CompareService compareService,
            SnapshotService snapshotService
    ) {
        this.workService = workService;
        this.connectService = connectService;
        this.compareService = compareService;
        this.snapshotService = snapshotService;
    }

    public List<WorkTarget> diff() {
        Work currentWork = workService.showWork();
        DbConfig dbConfig = connectService.findDbConfig(currentWork.getDbAlias());

        MetadataCollectService collectService = createMetadataCollectService(currentWork.getDbType());
        collectService.collect(dbConfig);

        WorkDiffResult result = compareService.compare(dbConfig.getDbConfigId());

        workService.saveDiffResult(dbConfig.getDbConfigId(), result);

        return workService.findWorkDiff(dbConfig.getDbConfigId());
    }

    protected MetadataCollectService createMetadataCollectService(String dbType) {
        MetadataCollector collector = createCollector(dbType);
        return new MetadataCollectService(collector, snapshotService);
    }

    private MetadataCollector createCollector(String dbType) {
        return switch (DbType.valueOf(dbType)) {
            case POSTGRESQL -> new PostgresMetadataCollector();
            default -> throw new IllegalArgumentException("지원하지 않는 DB 타입입니다: " + dbType);
        };
    }
}
