package io.dbflow.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DbFlowSecurityPathResolverTest {

    @Test
    void SQLite_DB_경로를_기준으로_security_master_key_경로를_결정한다() {
        assertEquals(
                Path.of("build/dbflow-test/security/master.key"),
                DbFlowSecurityPathResolver.resolveMasterKeyPath()
        );
    }
}
