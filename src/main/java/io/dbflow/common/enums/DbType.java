package io.dbflow.common.enums;

public enum DbType {
    POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://%s:%d/%s", "postgres");

    private final String driver;
    private final String urlPattern;
    private final String dir;

    DbType(String driver, String urlPattern, String dir) {
        this.driver = driver;
        this.urlPattern = urlPattern;
        this.dir = dir;
    }

    public String getDriver() {
        return driver;
    }

    public String getDir() {
        return dir;
    }

    public String createUrl(String host, int port, String databaseName) {
        return String.format(urlPattern, host, port, databaseName);
    }
}
