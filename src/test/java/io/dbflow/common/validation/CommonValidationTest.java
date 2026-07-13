package io.dbflow.common.validation;

import io.dbflow.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommonValidationTest {

    @Test
    void 필수_문자열은_null_빈값_공백을_허용하지_않는다() {
        assertThrows(ValidationException.class, () -> CommonValidation.required(null));
        assertThrows(ValidationException.class, () -> CommonValidation.required(""));
        assertThrows(ValidationException.class, () -> CommonValidation.required("   "));
        assertDoesNotThrow(() -> CommonValidation.required("value"));
    }

    @Test
    void 이메일_형식을_검증한다() {
        assertDoesNotThrow(() -> CommonValidation.validateEmail("user@example.com"));
        assertThrows(ValidationException.class, () -> CommonValidation.validateEmail("invalid-email"));
        assertThrows(ValidationException.class, () -> CommonValidation.validateEmail("user@example"));
    }

    @Test
    void Port는_1부터_65535까지_허용한다() {
        assertDoesNotThrow(() -> CommonValidation.validatePort(1));
        assertDoesNotThrow(() -> CommonValidation.validatePort(65535));
        assertThrows(ValidationException.class, () -> CommonValidation.validatePort(0));
        assertThrows(ValidationException.class, () -> CommonValidation.validatePort(65536));
    }

    @Test
    void DB별칭은_영문_숫자_하이픈_밑줄만_허용한다() {
        assertDoesNotThrow(() -> CommonValidation.validateAlias("local-db_01"));
        assertThrows(ValidationException.class, () -> CommonValidation.validateAlias("local db"));
        assertThrows(ValidationException.class, () -> CommonValidation.validateAlias("로컬"));
        assertThrows(ValidationException.class, () -> CommonValidation.validateAlias(""));
    }
}
