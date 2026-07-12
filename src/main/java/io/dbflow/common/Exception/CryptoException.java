package io.dbflow.common.Exception;

public class CryptoException extends RuntimeException {
    public static final String ENCRYPTION_FAILED = "DB 접속 비밀번호 암호화에 실패했습니다.";
    public static final String DECRYPTION_FAILED = "DB 접속 비밀번호 복호화에 실패했습니다.";
    public static final String KEY_LOAD_FAILED = "암호화 키를 불러오지 못했습니다.";

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
