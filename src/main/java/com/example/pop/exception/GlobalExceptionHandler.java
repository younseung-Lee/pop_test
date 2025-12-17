package com.example.pop.exception;

import com.example.pop.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 * 모든 컨트롤러에서 발생하는 예외를 일괄 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 401 Unauthorized - 인증되지 않은 사용자
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException e, 
            HttpServletRequest request) {
        
        log.warn("UnauthorizedException: {} - path: {}", e.getMessage(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                e.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    /**
     * 403 Forbidden - 권한 없음
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            ForbiddenException e, 
            HttpServletRequest request) {
        
        log.warn("ForbiddenException: {} - path: {}", e.getMessage(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                e.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /**
     * 404 Not Found - 리소스를 찾을 수 없음
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException e, 
            HttpServletRequest request) {
        
        log.warn("ResourceNotFoundException: {} - path: {}", e.getMessage(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                e.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * 400 Bad Request - 잘못된 요청
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            InvalidRequestException e, 
            HttpServletRequest request) {
        
        log.warn("InvalidRequestException: {} - path: {}", e.getMessage(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_REQUEST",
                e.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * 400 Bad Request - Validation 실패 (@Valid 사용 시)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, 
            HttpServletRequest request) {
        
        log.warn("MethodArgumentNotValidException - path: {}", request.getRequestURI());
        
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .value(error.getRejectedValue() != null ? error.getRejectedValue().toString() : "")
                        .reason(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("VALIDATION_FAILED")
                .message("입력값 검증에 실패했습니다.")
                .path(request.getRequestURI())
                .errors(fieldErrors)
                .build();
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * 400 Bad Request - 바인딩 에러
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException e, 
            HttpServletRequest request) {
        
        log.warn("BindException - path: {}", request.getRequestURI());
        
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .value(error.getRejectedValue() != null ? error.getRejectedValue().toString() : "")
                        .reason(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("BINDING_ERROR")
                .message("요청 파라미터 바인딩에 실패했습니다.")
                .path(request.getRequestURI())
                .errors(fieldErrors)
                .build();
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * 400 Bad Request - 필수 파라미터 누락
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e, 
            HttpServletRequest request) {
        
        log.warn("MissingServletRequestParameterException: {} - path: {}", e.getMessage(), request.getRequestURI());
        
        String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "MISSING_PARAMETER",
                message,
                request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * 400 Bad Request - 파라미터 타입 불일치
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e, 
            HttpServletRequest request) {
        
        log.warn("MethodArgumentTypeMismatchException: {} - path: {}", e.getMessage(), request.getRequestURI());
        
        String message = String.format("파라미터 '%s'의 타입이 올바르지 않습니다. (입력값: %s)", 
                e.getName(), e.getValue());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "TYPE_MISMATCH",
                message,
                request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * 400 Bad Request - JSON 파싱 에러
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e, 
            HttpServletRequest request) {
        
        log.warn("HttpMessageNotReadableException - path: {}", request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_JSON",
                "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.",
                request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * 404 Not Found - 핸들러를 찾을 수 없음
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException e, 
            HttpServletRequest request) {
        
        log.warn("NoHandlerFoundException: {} - path: {}", e.getMessage(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                "요청한 경로를 찾을 수 없습니다.",
                request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * 405 Method Not Allowed - 지원하지 않는 HTTP 메소드
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException e, 
            HttpServletRequest request) {
        
        log.warn("HttpRequestMethodNotSupportedException: {} - path: {}", e.getMessage(), request.getRequestURI());
        
        String message = String.format("'%s' 메소드는 지원하지 않습니다. 지원 메소드: %s", 
                e.getMethod(), String.join(", ", e.getSupportedMethods()));
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "METHOD_NOT_ALLOWED",
                message,
                request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(errorResponse);
    }

    /**
     * 413 Payload Too Large - 파일 크기 초과
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException e, 
            HttpServletRequest request) {
        
        log.warn("MaxUploadSizeExceededException - path: {}", request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "FILE_SIZE_EXCEEDED",
                "파일 크기가 너무 큽니다. 최대 업로드 크기를 확인해주세요.",
                request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(errorResponse);
    }

    /**
     * 500 Internal Server Error - 파일 업로드 에러
     */
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponse> handleFileUpload(
            FileUploadException e, 
            HttpServletRequest request) {
        
        log.error("FileUploadException: {} - path: {}", e.getMessage(), request.getRequestURI(), e);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "FILE_UPLOAD_ERROR",
                e.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * 500 Internal Server Error - 일반 RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException e, 
            HttpServletRequest request) {
        
        log.error("RuntimeException: {} - path: {}", e.getMessage(), request.getRequestURI(), e);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다.",
                request.getRequestURI()
        );
        
        // 개발 환경에서만 상세 정보 노출
        if (isDevelopmentMode()) {
            errorResponse.setDetail(e.getMessage());
        }
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }



    /**
     * 개발 모드 체크 (간단한 구현)
     * 실제로는 application.yml의 spring.profiles.active를 확인하는 것이 좋습니다
     */
    private boolean isDevelopmentMode() {
        return true; // 개발 중이므로 true로 설정
    }
}
