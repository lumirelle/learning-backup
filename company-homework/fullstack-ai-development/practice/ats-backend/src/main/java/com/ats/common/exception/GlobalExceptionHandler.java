package com.ats.common.exception;

import com.ats.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBiz(BizException ex) {
        ErrorCode ec = ex.getErrorCode();
        HttpStatus status = mapStatus(ec);
        log.warn("[BIZ] {} : {}", ec.getCode(), ex.getMessage());
        return ResponseEntity.status(status)
                .body(ApiResponse.fail(ec.getCode(), ex.getMessage()));
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuth(AuthenticationException ex) {
        log.warn("[AUTH] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMsg()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMsg()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleArgNotValid(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("[VALIDATION] {}", detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ErrorCode.VALIDATION_FAILED.getCode(), detail));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBind(BindException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ErrorCode.VALIDATION_FAILED.getCode(), detail));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleUpload(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.fail(ErrorCode.FILE_TOO_LARGE.getCode(), ErrorCode.FILE_TOO_LARGE.getMsg()));
    }

    /**
     * 路径不存在时 Spring 会 fall through 到静态资源 handler，再抛 NoResourceFoundException。
     * 默认会被 catch-all 兜底成 500，对前端体感很差（明明是 404 却报"服务器内部错误"）。
     * 单独 map 到 404 + 业务码 NOT_FOUND，让前端能正常处理。
     *
     * 同时也防止"后端进程未热加载新 Controller"这种事故被误诊为 500。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        log.warn("[404] {}", ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(ErrorCode.NOT_FOUND.getCode(), "接口不存在: " + ex.getResourcePath()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
        log.error("[UNKNOWN] ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMsg()));
    }

    private static HttpStatus mapStatus(ErrorCode ec) {
        int c = ec.getCode();
        if (c == ErrorCode.UNAUTHORIZED.getCode() || c == ErrorCode.INVALID_TOKEN.getCode()) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (c == ErrorCode.FORBIDDEN.getCode()
                || c == ErrorCode.JOB_ACCESS_DENIED.getCode()
                || c == ErrorCode.JOB_NOT_PUBLISHED.getCode()
                || c == ErrorCode.APPLICATION_ACCESS_DENIED.getCode()
                || c == ErrorCode.SELF_APPLY_FORBIDDEN.getCode()) {
            return HttpStatus.FORBIDDEN;
        }
        if (c == ErrorCode.FILE_NOT_FOUND.getCode()
                || c == ErrorCode.JOB_NOT_FOUND.getCode()
                || c == ErrorCode.TAG_NOT_FOUND.getCode()
                || c == ErrorCode.APPLICATION_NOT_FOUND.getCode()
                || c == ErrorCode.INTERVIEW_NOT_FOUND.getCode()) {
            return HttpStatus.NOT_FOUND;
        }
        if (c == ErrorCode.INTERVIEW_EDIT_FORBIDDEN.getCode()) {
            return HttpStatus.FORBIDDEN;
        }
        if (c == ErrorCode.FILE_TOO_LARGE.getCode()) {
            return HttpStatus.PAYLOAD_TOO_LARGE;
        }
        if (c == ErrorCode.FILE_TYPE_NOT_ALLOWED.getCode()) {
            return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        }
        if (c >= 40000 && c < 50000) {
            return HttpStatus.BAD_REQUEST;
        }
        if (c >= 20000 && c < 30000) {
            return HttpStatus.CONFLICT;
        }
        if (c >= 50000) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
