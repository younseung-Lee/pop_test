package com.example.pop.exception;

/**
 * 권한이 없는 사용자가 접근할 때 발생하는 예외
 * HTTP 403 Forbidden
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
