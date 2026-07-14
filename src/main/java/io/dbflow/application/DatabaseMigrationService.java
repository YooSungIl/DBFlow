package io.dbflow.application;

import io.dbflow.common.DbFlowVersion;
import io.dbflow.common.exception.ServiceException;
import io.dbflow.domain.MigrationScript;
import io.dbflow.infrastructure.migration.MigrationScriptLoader;
import io.dbflow.infrastructure.repository.InstallRepository;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class DatabaseMigrationService {

    private final InstallRepository installRepository;
    private final MigrationScriptLoader scriptLoader;
    private final String appVersion;

    public DatabaseMigrationService(InstallRepository installRepository) {
        this(installRepository, new MigrationScriptLoader(), DbFlowVersion.getAppVersion());
    }

    public DatabaseMigrationService(
            InstallRepository installRepository,
            MigrationScriptLoader scriptLoader
    ) {
        this(installRepository, scriptLoader, DbFlowVersion.getAppVersion());
    }

    DatabaseMigrationService(
            InstallRepository installRepository,
            MigrationScriptLoader scriptLoader,
            String appVersion
    ) {
        this.installRepository = installRepository;
        this.scriptLoader = scriptLoader;
        this.appVersion = appVersion;
    }

    public void migrate(Path databasePath) {
        int[] parsedAppVersion = parseVersion(appVersion);
        List<MigrationScript> targetScripts = scriptLoader.load().stream()
                .filter(script -> script.majorVersion() == parsedAppVersion[0])
                .filter(script -> script.minorVersion() <= parsedAppVersion[1])
                .sorted(
                        Comparator.comparingInt(MigrationScript::minorVersion)
                                .thenComparingInt(MigrationScript::patchVersion)
                )
                .toList();

        if (targetScripts.isEmpty()) {
            throw new ServiceException(ServiceException.MIGRATION_SCRIPT_NOT_FOUND);
        }

        installRepository.executeMigrations(databasePath, appVersion, targetScripts);
    }

    private int[] parseVersion(String version) {
        String[] values = version.split("\\.");
        if (values.length != 3) {
            throw new ServiceException(ServiceException.INVALID_APP_VERSION);
        }

        try {
            return new int[]{
                    Integer.parseInt(values[0]),
                    Integer.parseInt(values[1]),
                    Integer.parseInt(values[2])
            };
        } catch (NumberFormatException e) {
            throw new ServiceException(ServiceException.INVALID_APP_VERSION, e);
        }
    }
}
