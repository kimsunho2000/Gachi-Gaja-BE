package com.Gachi_Gaja.server.auth.config;

import com.Gachi_Gaja.server.auth.exception.*;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.Gachi_Gaja.server.auth")
public class AuthExceptionHandler {

    private record ErrorBody(String message, String code, Map<String,String> errors) {}

    // 400: Bean Validation 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBody> handleValidation(MethodArgumentNotValidException ex) {
        Map<String,String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a,b)->a));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorBody("잘못된 요청 형식입니다.", "Bad Request", fields));
    }

    // 401: 비밀번호 불일치
    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<ErrorBody> handleWrongPassword(WrongPasswordException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorBody(ex.getMessage(), "Wrong Passwd", null));
    }

    // 404: 대상 없음
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorBody> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorBody(ex.getMessage(), "Not Found", null));
    }

    // 409: 충돌(중복)
    @ExceptionHandler({EmailAlreadyUsedException.class, NicknameAlreadyUsedException.class})
    public ResponseEntity<ErrorBody> handleConflict(RuntimeException ex) {
        String code = (ex instanceof EmailAlreadyUsedException) ? "Email Already Used" : "Nickname Already Used";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorBody(ex.getMessage(), code, null));
    }
}
