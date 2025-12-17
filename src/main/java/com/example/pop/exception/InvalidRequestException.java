package com.example.pop.exception;

/**
 * 잘못된 요청 파라미터로 인한 예외
 * HTTP 400 Bad Request
 */
public class InvalidRequestException extends RuntimeException {
    
    public InvalidRequestException(String message) {
        super(message);
    }
    
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
