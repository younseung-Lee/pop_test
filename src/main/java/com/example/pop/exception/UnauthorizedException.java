package com.example.pop.exception;

/**
 * 인증되지 않은 사용자가 접근할 때 발생하는 예외
 * HTTP 401 Unauthorized
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
