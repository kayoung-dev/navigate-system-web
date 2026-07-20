package com.example.path.util;

/**
 * JDBC 저장소 구현체에서 발생하는 체크 예외(SQLException 등)를 감싸는 언체크 예외.
 * repository 인터페이스 메서드가 체크 예외를 선언하지 않으므로 이 예외로 통일해서 던진다.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
