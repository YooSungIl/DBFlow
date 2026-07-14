package io.dbflow.infrastructure.path;

import java.nio.file.Path;

public final class DbFlowPathResolver {

    public static final String USER_DATA_DIRECTORY_PROPERTY = "dbflow.userDataDirectory";

    private DbFlowPathResolver() {
    }

    public static Path resolveUserDataDirectory() {
        String configuredPath = System.getProperty(USER_DATA_DIRECTORY_PROPERTY);
        if (configuredPath != null && !configuredPath.isBlank()) {
            return Path.of(configuredPath).toAbsolutePath().normalize();
        }
        return Path.of(System.getProperty("user.home"), ".dbflow").toAbsolutePath().normalize();
    }

    public static Path resolveDatabasePath() {
        return resolveUserDataDirectory().resolve("data").resolve("dbflow.db");
    }

    public static Path resolveMasterKeyPath() {
        return resolveUserDataDirectory().resolve("security").resolve("master.key");
    }
}
