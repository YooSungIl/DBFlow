package io.dbflow.common.exception;

public class ServiceException extends RuntimeException {
    public static final String USER_NOT_FOUND = "등록된 사용자 정보가 없습니다.";
    public static final String DB_CONFIG_NOT_FOUND = "등록된 DB 접속정보가 없습니다.";
    public static final String WORK_NOT_FOUND = "DB작업 공간 정보가 없습니다.";
    public static final String COMMIT_TITLE_REQUIRED = "커밋 제목을 입력해주세요.";
    public static final String COMMIT_WORK_NOT_FOUND = "커밋할 변경내역이 없습니다. 먼저 dbf diff 명령어를 실행해주세요.";
    public static final String INSTALL_DIRECTORY_ALREADY_EXISTS = "DBFlow 사용자 데이터 디렉터리가 이미 존재합니다: ~/.dbflow";
    public static final String INSTALL_DIRECTORY_CREATION_FAILED = "DBFlow 사용자 데이터 디렉터리를 생성하지 못했습니다.";
    public static final String INSTALL_DATABASE_ALREADY_EXISTS = "DBFlow 제품 데이터베이스가 이미 존재합니다: ~/.dbflow/data/dbflow.db";
    public static final String MIGRATION_SCRIPT_LOAD_FAILED = "DBFlow DB 마이그레이션 파일을 불러오지 못했습니다.";
    public static final String MIGRATION_SCRIPT_NOT_FOUND = "현재 제품 버전에 해당하는 DB 마이그레이션 파일이 없습니다.";
    public static final String INVALID_APP_VERSION = "DBFlow 제품 버전 형식이 올바르지 않습니다.";

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
