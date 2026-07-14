package io.dbflow.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DbFlowVersionTest {

    @Test
    void Gradle에서_생성한_제품_버전을_읽는다() {
        assertTrue(DbFlowVersion.getAppVersion().matches("\\d+\\.\\d+\\.\\d+"));
    }
}
