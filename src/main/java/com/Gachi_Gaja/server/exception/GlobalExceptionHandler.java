package com.Gachi_Gaja.server.exception;

import com.Gachi_Gaja.server.dto.response.ErrorResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponseDTO(status.value(), message));
    }

    // 400 - 잘못된 요청 (누락/형식오류/검증 실패)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("400 IllegalArgumentException: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // @Validated 에서 체크된 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        if (message.isBlank()) message = "Validation failed";
        log.warn("400 MethodArgumentNotValid: {}", message);
        return build(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponseDTO> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        if (message.isBlank()) message = ex.getMessage();
        log.warn("400 BindException: {}", message);
        return build(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        if (message.isBlank()) message = ex.getMessage();
        log.warn("400 ConstraintViolation: {}", message);
        return build(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingPathVariableException.class, MissingRequestHeaderException.class})
    public ResponseEntity<ErrorResponseDTO> handleMissing(Exception ex) {
        log.warn("400 Missing parameter/header/path: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "";
        String msg = String.format("Parameter '%s' type mismatch. Required: %s", name, required);
        log.warn("400 TypeMismatch: {}", msg);
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("400 NotReadable: {}", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request");
    }

    // 401/403 - 인증/인가
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthentication(AuthenticationException ex) {
        log.warn("401 AuthenticationException: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<ErrorResponseDTO> handleWrongPassword(WrongPasswordException ex) {
        log.warn("401 Wrong Passwd: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex) {
        log.warn("403 AccessDenied: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // 404 - 리소스 없음 (그룹/멤버/요구사항 등)
    @ExceptionHandler({ NoSuchElementException.class, EntityNotFoundException.class, NotFoundException.class })
    public ResponseEntity<ErrorResponseDTO> handleNotFound(Exception ex) {
        log.warn("404 NotFound: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 405 - 메서드 미지원
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("405 MethodNotSupported: {}", ex.getMessage());
        return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    // 409 - 중복/무결성 위반
    // 추후 중복 오류 통일 예정
    @ExceptionHandler({ DataIntegrityViolationException.class, AlreadyExistsException.class, NicknameAlreadyUsedException.class, EmailAlreadyUsedException.class })
    public ResponseEntity<ErrorResponseDTO> handleConflict(Exception ex) {
        log.warn("409 Conflict: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 500 - 그 외 서버 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAll(Exception ex, HttpServletRequest req) {
        log.error("500 Internal error on {}: ", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }
}
