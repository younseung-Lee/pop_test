package com.example.pop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 에러 응답 공통 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * HTTP 상태 코드
     */
    private int status;
    
    /**
     * 에러 코드 (선택사항)
     */
    private String code;
    
    /**
     * 에러 메시지
     */
    private String message;
    
    /**
     * 상세 에러 메시지 (개발용, production에서는 제외)
     */
    private String detail;
    
    /**
     * 에러 발생 경로
     */
    private String path;
    
    /**
     * 에러 발생 시간
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Validation 에러 상세 정보
     */
    private List<FieldError> errors;
    
    /**
     * Validation 에러를 위한 필드 에러 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String value;
        private String reason;
    }
    
    /**
     * 간단한 에러 응답 생성
     */
    public static ErrorResponse of(int status, String message) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .build();
    }
    
    /**
     * 상세 에러 응답 생성
     */
    public static ErrorResponse of(int status, String code, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .code(code)
                .message(message)
                .path(path)
                .build();
    }
}
