package io.dbflow.application;

import io.dbflow.infrastructure.external.repository.MetadataCollector;
import io.dbflow.infrastructure.repository.CommitRepository;
import io.dbflow.infrastructure.repository.DbConfigRepository;
import io.dbflow.infrastructure.repository.SnapshotRepository;
import io.dbflow.infrastructure.repository.UserRepository;
import io.dbflow.infrastructure.repository.WorkRepository;

public final class ServiceFactory {

    private ServiceFactory() {
    }

    public static UserService userService() {
        return new UserService(new UserRepository());
    }

    public static ConnectService connectService() {
        return new ConnectService(new DbConfigRepository());
    }

    public static WorkService workService() {
        return new WorkService(
                new DbConfigRepository(),
                new UserRepository(),
                new WorkRepository()
        );
    }

    public static SnapshotService snapshotService() {
        return new SnapshotService(new SnapshotRepository());
    }

    public static CompareService compareService() {
        return new CompareService(snapshotService());
    }

    public static MetadataCollectService metadataCollectService(MetadataCollector metadataCollector) {
        return new MetadataCollectService(metadataCollector, snapshotService());
    }

    public static CommitService commitService() {
        return new CommitService(
                new CommitRepository(),
                new SnapshotRepository(),
                userService(),
                workService()
        );
    }

    public static DiffService diffService() {
        SnapshotService snapshotService = snapshotService();

        return new DiffService(
                workService(),
                connectService(),
                new CompareService(snapshotService),
                snapshotService
        );
    }
}
