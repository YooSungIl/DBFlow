package io.dbflow.common.exception;

public class ServiceException extends RuntimeException {
    public static final String USER_NOT_FOUND = "등록된 사용자 정보가 없습니다.";
    public static final String DB_CONFIG_NOT_FOUND = "등록된 DB 접속정보가 없습니다.";
    public static final String WORK_NOT_FOUND = "DB작업 공간 정보가 없습니다.";
    public static final String COMMIT_TITLE_REQUIRED = "커밋 제목을 입력해주세요.";
    public static final String COMMIT_WORK_NOT_FOUND = "커밋할 변경내역이 없습니다. 먼저 dbf diff 명령어를 실행해주세요.";

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
