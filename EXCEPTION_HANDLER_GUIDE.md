# GlobalExceptionHandler 테스트 가이드

## 구현 완료 항목

### 1. 커스텀 예외 클래스
- ✅ `UnauthorizedException` - 인증 실패 (401)
- ✅ `ForbiddenException` - 권한 없음 (403)
- ✅ `ResourceNotFoundException` - 리소스 없음 (404)
- ✅ `InvalidRequestException` - 잘못된 요청 (400)
- ✅ `FileUploadException` - 파일 업로드 에러 (500)

### 2. 에러 응답 DTO
- ✅ `ErrorResponse` - 표준화된 에러 응답 구조
  - status: HTTP 상태 코드
  - code: 에러 코드
  - message: 사용자 친화적 메시지
  - detail: 개발용 상세 정보 (선택)
  - path: 요청 경로
  - timestamp: 발생 시간
  - errors: Validation 에러 상세 정보

### 3. GlobalExceptionHandler
다음 예외들을 처리합니다:
- ✅ 401: UnauthorizedException
- ✅ 403: ForbiddenException
- ✅ 404: ResourceNotFoundException, NoHandlerFoundException
- ✅ 400: InvalidRequestException, MethodArgumentNotValidException, BindException, MissingServletRequestParameterException, MethodArgumentTypeMismatchException, HttpMessageNotReadableException
- ✅ 405: HttpRequestMethodNotSupportedException
- ✅ 413: MaxUploadSizeExceededException
- ✅ 500: FileUploadException, RuntimeException, Exception

## 테스트 방법

### 1. 인증 실패 테스트 (401)
```bash
# 로그인하지 않은 상태로 템플릿 조회
curl -X GET "http://localhost:8080/api/templates/my"
```

**예상 응답:**
```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "로그인이 필요합니다.",
  "path": "/api/templates/my",
  "timestamp": "2025-01-15T10:30:00"
}
```

### 2. 권한 없음 테스트 (403)
```bash
# 일반 사용자가 공통 템플릿 등록 시도
curl -X POST "http://localhost:8080/api/templates/common" \
  -F "templateName=test" \
  -F "layoutType=VERTICAL" \
  -F "templateImages=@image.jpg"
```

**예상 응답:**
```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "공통 템플릿 등록 권한이 없습니다. (관리자 전용)",
  "path": "/api/templates/common",
  "timestamp": "2025-01-15T10:30:00"
}
```

### 3. 잘못된 요청 테스트 (400)
```bash
# 필수 파라미터 누락
curl -X POST "http://localhost:8080/api/templates/my" \
  -H "Content-Type: application/json" \
  -d '{}'
```

**예상 응답:**
```json
{
  "status": 400,
  "code": "INVALID_REQUEST",
  "message": "템플릿 이름은 필수입니다.",
  "path": "/api/templates/my",
  "timestamp": "2025-01-15T10:30:00"
}
```

### 4. JSON 파싱 에러 테스트 (400)
```bash
# 잘못된 JSON 형식
curl -X POST "http://localhost:8080/api/templates/my" \
  -H "Content-Type: application/json" \
  -d '{invalid json}'
```

**예상 응답:**
```json
{
  "status": 400,
  "code": "INVALID_JSON",
  "message": "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.",
  "path": "/api/templates/my",
  "timestamp": "2025-01-15T10:30:00"
}
```

### 5. 잘못된 HTTP 메소드 테스트 (405)
```bash
# POST만 허용하는 엔드포인트에 GET 요청
curl -X GET "http://localhost:8080/api/templates/my" \
  -H "Content-Type: application/json"
```

**예상 응답:**
```json
{
  "status": 405,
  "code": "METHOD_NOT_ALLOWED",
  "message": "'GET' 메소드는 지원하지 않습니다. 지원 메소드: POST",
  "path": "/api/templates/my",
  "timestamp": "2025-01-15T10:30:00"
}
```

## 기존 코드 변경 사항

### TemplateController.java
- `RuntimeException` → `UnauthorizedException`, `ForbiddenException`, `InvalidRequestException`으로 변경
- 더 명확한 에러 메시지 제공

### TemplateServiceImpl.java
- `RuntimeException` → `InvalidRequestException`으로 변경
- Validation 로직에 커스텀 예외 적용

## 장점

1. **일관된 에러 응답**: 모든 API에서 동일한 형식의 에러 응답
2. **명확한 에러 구분**: HTTP 상태 코드와 커스텀 에러 코드로 정확한 원인 파악
3. **개발자 친화적**: 개발 환경에서는 상세 에러 정보 제공
4. **유지보수 용이**: 예외 처리 로직이 한 곳에 집중
5. **확장 가능**: 새로운 예외 타입 추가 용이

## 다음 단계

1. **Spring Validation 적용** (@Valid, @NotBlank 등)
2. **환경별 에러 상세 정보 제어** (dev/prod)
3. **에러 로깅 강화** (Sentry, ELK 등 연동)
4. **API 문서화** (Swagger에 에러 응답 명시)
5. **통합 테스트 작성**
