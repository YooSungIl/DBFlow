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

    public List<WorkTarget> diff() {
        WorkService workService = new WorkService();
        Work currentWork = workService.showWork();

        ConnectService connectService = new ConnectService(new DbConfigRepository());
        DbConfig dbConfig = connectService.findDbConfig(currentWork.getDbAlias());

        MetadataCollector collector = createCollector(currentWork.getDbType());
        MetadataCollectService collectService = new MetadataCollectService(collector);
        collectService.collect(dbConfig);

        CompareService compareService = new CompareService();
        WorkDiffResult result = compareService.compare(dbConfig.getDbConfigId());

        workService.diffResult(dbConfig.getDbConfigId(), result);

        return workService.findWorkDiff(dbConfig.getDbConfigId());
    }

    private MetadataCollector createCollector(String dbType) {
        return switch (DbType.valueOf(dbType)) {
            case POSTGRESQL -> new PostgresMetadataCollector();
            default -> throw new IllegalArgumentException("지원하지 않는 DB 타입입니다: " + dbType);
        };
    }
}
