package io.dbflow.application;

import io.dbflow.domain.ColumnMetadata;
import io.dbflow.domain.DbConfig;
import io.dbflow.domain.TableMetadata;
import io.dbflow.domain.TableSnapshot;
import io.dbflow.domain.Work;
import io.dbflow.domain.WorkDiffResult;
import io.dbflow.domain.WorkTarget;
import io.dbflow.infrastructure.external.repository.MetadataCollector;
import io.dbflow.infrastructure.repository.DbConfigRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiffServiceTest {

    @Test
    void 수집_비교_Work저장_조회_순서로_Diff를_수행한다() {
        DbConfig dbConfig = dbConfig();
        Work work = work();
        WorkDiffResult comparedResult = new WorkDiffResult();
        WorkTarget target = new WorkTarget(100L, "TABLE", "member", "회원", "ADD");
        comparedResult.addTarget(target);
        StubWorkService workService = new StubWorkService(work, List.of(target));
        StubMetadataCollectService collectService = new StubMetadataCollectService(false);
        DiffService service = new TestDiffService(
                workService,
                new StubConnectService(dbConfig),
                new StubCompareService(comparedResult),
                collectService
        );

        List<WorkTarget> result = service.diff();

        assertTrue(collectService.collected);
        assertSame(dbConfig, collectService.collectedDbConfig);
        assertEquals(100L, workService.savedDbConfigId);
        assertSame(comparedResult, workService.savedResult);
        assertEquals(List.of(target), result);
    }

    @Test
    void 메타데이터_수집에_실패하면_비교와_Work저장을_수행하지_않는다() {
        StubWorkService workService = new StubWorkService(work(), List.of());
        StubCompareService compareService = new StubCompareService(new WorkDiffResult());
        DiffService service = new TestDiffService(
                workService,
                new StubConnectService(dbConfig()),
                compareService,
                new StubMetadataCollectService(true)
        );

        assertThrows(IllegalStateException.class, service::diff);
        assertEquals(0, compareService.compareCount);
        assertNull(workService.savedResult);
    }

    private Work work() {
        Work work = new Work();
        work.setDbAlias("local");
        work.setDbType("POSTGRESQL");
        return work;
    }

    private DbConfig dbConfig() {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setDbConfigId(100L);
        dbConfig.setDbAlias("local");
        return dbConfig;
    }

    private static class TestDiffService extends DiffService {
        private final MetadataCollectService collectService;

        private TestDiffService(
                WorkService workService,
                ConnectService connectService,
                CompareService compareService,
                MetadataCollectService collectService
        ) {
            super(workService, connectService, compareService, new SnapshotService());
            this.collectService = collectService;
        }

        @Override
        protected MetadataCollectService createMetadataCollectService(String dbType) {
            return collectService;
        }
    }

    private static class StubWorkService extends WorkService {
        private final Work work;
        private final List<WorkTarget> storedTargets;
        private Long savedDbConfigId;
        private WorkDiffResult savedResult;

        private StubWorkService(Work work, List<WorkTarget> storedTargets) {
            this.work = work;
            this.storedTargets = storedTargets;
        }

        @Override
        public Work showWork() {
            return work;
        }

        @Override
        public void saveDiffResult(Long dbConfigId, WorkDiffResult result) {
            savedDbConfigId = dbConfigId;
            savedResult = result;
        }

        @Override
        public List<WorkTarget> findWorkDiff(Long dbConfigId) {
            return storedTargets;
        }
    }

    private static class StubConnectService extends ConnectService {
        private final DbConfig dbConfig;

        private StubConnectService(DbConfig dbConfig) {
            super(new DbConfigRepository());
            this.dbConfig = dbConfig;
        }

        @Override
        public DbConfig findDbConfig(String dbAlias) {
            return dbConfig;
        }
    }

    private static class StubCompareService extends CompareService {
        private final WorkDiffResult result;
        private int compareCount;

        private StubCompareService(WorkDiffResult result) {
            this.result = result;
        }

        @Override
        public WorkDiffResult compare(Long dbConfigId) {
            compareCount++;
            return result;
        }
    }

    private static class StubMetadataCollectService extends MetadataCollectService {
        private final boolean fail;
        private boolean collected;
        private DbConfig collectedDbConfig;

        private StubMetadataCollectService(boolean fail) {
            super(new EmptyMetadataCollector());
            this.fail = fail;
        }

        @Override
        public void collect(DbConfig dbConfig) {
            if (fail) {
                throw new IllegalStateException("collect failed");
            }
            collected = true;
            collectedDbConfig = dbConfig;
        }
    }

    private static class EmptyMetadataCollector implements MetadataCollector {
        @Override
        public List<TableMetadata> collectTableSnapshotList(DbConfig dbConfig) {
            return List.of();
        }

        @Override
        public List<ColumnMetadata> collectColumnSnapshotList(DbConfig dbConfig, List<TableSnapshot> tableSnapshot) {
            return List.of();
        }
    }
}
