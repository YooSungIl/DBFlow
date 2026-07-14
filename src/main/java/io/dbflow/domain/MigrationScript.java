package io.dbflow.domain;

public record MigrationScript(
        int majorVersion,
        int minorVersion,
        int patchVersion,
        String scriptName,
        String checksum,
        String sql
) {
}
