package io.dbflow.infrastructure.security;

import io.dbflow.common.exception.CryptoException;
import org.apache.ibatis.io.Resources;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public final class DbFlowSecurityPathResolver {

    private static final String PROPERTIES_RESOURCE = "dbflow.properties";
    private static final String SQLITE_URL_PREFIX = "jdbc:sqlite:";

    private DbFlowSecurityPathResolver() {
    }

    public static Path resolveMasterKeyPath() {
        try {
            Properties properties = new Properties();
            try (InputStream inputStream = Resources.getResourceAsStream(PROPERTIES_RESOURCE)) {
                properties.load(inputStream);
            }

            String databaseUrl = properties.getProperty("db.url");
            if (databaseUrl == null || !databaseUrl.startsWith(SQLITE_URL_PREFIX)) {
                throw new IllegalStateException("SQLite DB 경로를 확인할 수 없습니다.");
            }

            Path databasePath = Path.of(databaseUrl.substring(SQLITE_URL_PREFIX.length()));
            Path databaseDirectory = databasePath.getParent();
            if (databaseDirectory == null) {
                throw new IllegalStateException("SQLite DB 상위 경로를 확인할 수 없습니다.");
            }

            Path dbFlowDirectory = "data".equals(databaseDirectory.getFileName().toString())
                    ? databaseDirectory.getParent()
                    : databaseDirectory;
            if (dbFlowDirectory == null) {
                throw new IllegalStateException("DBFlow 경로를 확인할 수 없습니다.");
            }

            return dbFlowDirectory.resolve("security").resolve("master.key").normalize();
        } catch (Exception e) {
            throw new CryptoException(CryptoException.KEY_LOAD_FAILED, e);
        }
    }
}
