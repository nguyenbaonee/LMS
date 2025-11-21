package com.example.LMS.Exception;

import com.example.LMS.dto.ApiResponse;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    MessageSource messageSource;
    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException e, Locale locale) {
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(messageSource.getMessage(errorCode.getMessage(), null, locale));
        return ResponseEntity.badRequest().body(apiResponse);
    }
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse> handleMultipartException(MultipartException ex, Locale locale) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .message(messageSource.getMessage("valid.multipartFile.required", null, locale))
                        .build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse> handleMissingParam(MissingServletRequestParameterException ex, Locale locale) {
        return ResponseEntity.badRequest().body(ApiResponse.builder()
                .message(messageSource.getMessage("valid.multipartFile.required", null, locale))
                .build());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse> handleMissingPart(MissingServletRequestPartException ex, Locale locale) {
        return ResponseEntity.badRequest().body(ApiResponse.builder()
                .message(messageSource.getMessage("valid.multipartFile.required", null, locale))
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex, Locale locale) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String msg = messageSource.getMessage(error, locale);
            errors.put(error.getField(), msg);
        });

        return ResponseEntity.badRequest().body(errors);
    }
}
