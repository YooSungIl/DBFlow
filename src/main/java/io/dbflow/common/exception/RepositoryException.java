package io.dbflow.common.exception;

public class RepositoryException extends RuntimeException {
    public static final String COMMIT_NOT_FOUND = "등록된 커밋 정보가 없습니다.";
    public static final String COMMIT_TARGET_NOT_FOUND = "해당 커밋에 대상 오브젝트가 없습니다.";
    public static final String COMMIT_COMPONENT_NOT_FOUND = "등록된 구성 요소 정보가 없습니다.";

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
