package com.ecommerce.project.exception;

import com.ecommerce.project.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> catchAll(Exception ex) {
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        log.warn("Unhandled exceptionL {}", ex.getMessage());
        return ResponseEntity.status(500).body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException ex) {
        log.warn("Conflict occurred: {}", ex.getMessage());

        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);

        return ResponseEntity.status(409).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            ResourceNotFoundException ex
    ) {
        log.warn("Resource Not Found: {}", ex.getMessage());

        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);

        return ResponseEntity.status(404).body(response);
    }
}
