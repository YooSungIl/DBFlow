package io.dbflow.common.Exception;

public class ValidationException extends RuntimeException {
    public static final String DUPLICATED_DB_ALIAS = "이미 존재하는 DB 별칭입니다.";
    public static final String UNSUPPORTED_DBMS = "지원하지 않는 DBMS입니다.";

    public ValidationException(String message) {
        super(message);
    }
}
