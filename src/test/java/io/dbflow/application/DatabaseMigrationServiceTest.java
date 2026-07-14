package io.dbflow.application;

import io.dbflow.domain.MigrationScript;
import io.dbflow.infrastructure.migration.MigrationScriptLoader;
import io.dbflow.infrastructure.repository.InstallRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseMigrationServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void 같은_MAJOR의_제품_MINOR_이하_SQL을_MINOR와_PATCH_순서로_실행한다() {
        CapturingInstallRepository repository = new CapturingInstallRepository();
        MigrationScriptLoader loader = new MigrationScriptLoader() {
            @Override
            public List<MigrationScript> load() {
                return List.of(
                        script(1, 2, 1),
                        script(1, 1, 1),
                        script(1, 3, 0),
                        script(1, 2, 0),
                        script(2, 0, 0),
                        script(1, 1, 0)
                );
            }
        };
        DatabaseMigrationService service = new DatabaseMigrationService(repository, loader, "1.2.0");

        service.migrate(temporaryDirectory.resolve("dbflow.db"));

        assertEquals("1.2.0", repository.appVersion);
        assertEquals(
                List.of("1.1.0", "1.1.1", "1.2.0", "1.2.1"),
                repository.schemaVersions
        );
    }

    private MigrationScript script(int major, int minor, int patch) {
        return new MigrationScript(
                major,
                minor,
                patch,
                "V" + major + "." + minor + "." + patch + "__test.sql",
                "checksum",
                "SELECT 1;"
        );
    }

    private static class CapturingInstallRepository extends InstallRepository {
        private String appVersion;
        private final List<String> schemaVersions = new ArrayList<>();

        @Override
        public void executeMigrations(
                Path databasePath,
                String appVersion,
                List<MigrationScript> scripts
        ) {
            this.appVersion = appVersion;
            scripts.forEach(script -> schemaVersions.add(
                    script.majorVersion() + "." + script.minorVersion() + "." + script.patchVersion()
            ));
        }
    }
}
