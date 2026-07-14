package io.dbflow.common;

import java.io.InputStream;
import java.util.Properties;

public final class DbFlowVersion {

    private static final String VERSION_RESOURCE = "META-INF/dbflow-version.properties";
    private static final String VERSION_PROPERTY = "app.version";
    private static final String APP_VERSION = loadAppVersion();

    private DbFlowVersion() {
    }

    public static String getAppVersion() {
        return APP_VERSION;
    }

    private static String loadAppVersion() {
        try (InputStream inputStream = DbFlowVersion.class.getClassLoader()
                .getResourceAsStream(VERSION_RESOURCE)) {
            if (inputStream == null) {
                throw new IllegalStateException("DBFlow 제품 버전 리소스를 찾을 수 없습니다.");
            }

            Properties properties = new Properties();
            properties.load(inputStream);
            String version = properties.getProperty(VERSION_PROPERTY);
            if (version == null || version.isBlank()) {
                throw new IllegalStateException("DBFlow 제품 버전이 설정되지 않았습니다.");
            }
            return version;
        } catch (Exception e) {
            throw new IllegalStateException("DBFlow 제품 버전을 불러오지 못했습니다.", e);
        }
    }
}
