package io.dbflow.infrastructure.path;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DbFlowPathResolverTest {

    @Test
    void 모든_사용자_데이터_경로를_동일한_루트에서_결정한다() {
        Path userDataDirectory = DbFlowPathResolver.resolveUserDataDirectory();

        assertEquals(
                userDataDirectory.resolve("data/dbflow.db"),
                DbFlowPathResolver.resolveDatabasePath()
        );
        assertEquals(
                userDataDirectory.resolve("security/master.key"),
                DbFlowPathResolver.resolveMasterKeyPath()
        );
    }
}
