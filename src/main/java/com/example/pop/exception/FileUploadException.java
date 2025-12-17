package com.example.pop.exception;

/**
 * 파일 업로드/처리 중 발생하는 예외
 * HTTP 500 Internal Server Error
 */
public class FileUploadException extends RuntimeException {
    
    public FileUploadException(String message) {
        super(message);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
