package io.dbflow.common.validation;

import io.dbflow.common.Exception.ValidationException;

public class CommonValidation {

    public static void required(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("값을 입력해 주세요.");
        }
    }

    public static void requiredInt(Integer value) {
        if (value == null) {
            throw new ValidationException("값을 입력해 주세요.");
        }
    }

    public static void validateEmail(String email) {

        required(email);

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("올바른 이메일 형식이 아닙니다.");
        }
    }

    public static void validatePort(Integer port) {
        if (port < 1 || port > 65535) {
            throw new ValidationException("Port는 1 ~ 65535 범위여야 합니다.");
        }
    }

    public static void validateAlias(String alias) {

        if (alias == null || alias.isBlank()) {
            throw new ValidationException("DB 별칭을 입력해 주세요.");
        }

        if (!alias.matches("^[a-zA-Z0-9_-]+$")) {
            throw new ValidationException(
                    "DB 별칭은 영문, 숫자, -, _ 만 사용할 수 있습니다."
            );
        }
    }
}
