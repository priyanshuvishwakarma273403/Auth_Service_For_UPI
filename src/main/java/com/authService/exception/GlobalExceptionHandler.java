package com.authService.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ================================================================
 * Global Exception Handler
 * ================================================================
// * @RestControllerAdvice - saare controllers ke exceptions yahan catch honge
 *
 * Production mein:
 * - Stack trace kabhi client ko mat bhejo
 * - Generic error messages rakho (security)
 * - Sabh errors log karo (monitoring ke liye)
 * ================================================================
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Custom Auth exceptions handle karo
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException e) {

        log.warn("Auth Exception :  {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    /**
     * Validation errors handle karo (@Valid se aane wale)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException e) {

        Map<String, Object> fieldErrors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("error", "Validation Failed");
        response.put("fieldErrors", fieldErrors);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bad credentials handle karo
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("Invalid credentials", 401));
    }

    /**
     * Generic exceptions handle karo - client ko details mat batao
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected error: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("Internal server error. Please try again later.", 500));
    }

    // Error Response record
    public record ErrorResponse(String message, int status, LocalDateTime timestamp) {
        public static ErrorResponse of(String message, int status) {
            return new ErrorResponse(message, status, LocalDateTime.now());
        }
    }
}
