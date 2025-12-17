package com.example.pop.exception;

/**
 * 요청한 리소스를 찾을 수 없을 때 발생하는 예외
 * HTTP 404 Not Found
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
