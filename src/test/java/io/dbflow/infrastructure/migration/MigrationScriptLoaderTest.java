package io.dbflow.infrastructure.migration;

import io.dbflow.domain.MigrationScript;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MigrationScriptLoaderTest {

    @Test
    void 마이그레이션_파일명과_체크섬을_읽는다() {
        List<MigrationScript> scripts = new MigrationScriptLoader().load();
        MigrationScript initialScript = scripts.stream()
                .filter(script -> script.scriptName().equals("V1.0.0__initial_schema.sql"))
                .findFirst()
                .orElseThrow();

        assertEquals(1, initialScript.majorVersion());
        assertEquals(0, initialScript.minorVersion());
        assertEquals(0, initialScript.patchVersion());
        assertEquals(64, initialScript.checksum().length());
        assertFalse(initialScript.sql().isBlank());
    }
}
