package com.library.exception;

import com.library.dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<BaseResponse<Object>> handleBookNotFoundException(BookNotFoundException ex) {
        log.error("Book not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(BaseResponse.error("BOOK_NOT_FOUND"));
    }
    
    @ExceptionHandler(DuplicateBookException.class)
    public ResponseEntity<BaseResponse<Object>> handleDuplicateBookException(DuplicateBookException ex) {
        log.error("Duplicate book: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(BaseResponse.error("DUPLICATE_BOOK"));
    }
    
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<BaseResponse<Object>> handleInsufficientStockException(InsufficientStockException ex) {
        log.error("Insufficient stock: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error("INSUFFICIENT_STOCK"));
    }
    
    @ExceptionHandler(MaxLoansExceededException.class)
    public ResponseEntity<BaseResponse<Object>> handleMaxLoansExceededException(MaxLoansExceededException ex) {
        log.error("Max loans exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error("MAX_LOANS_EXCEEDED"));
    }
    
    @ExceptionHandler(BookNotAvailableException.class)
    public ResponseEntity<BaseResponse<Object>> handleBookNotAvailableException(BookNotAvailableException ex) {
        log.error("Book not available: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error("BOOK_NOT_AVAILABLE"));
    }
    
    @ExceptionHandler(DuplicateLoanRequestException.class)
    public ResponseEntity<BaseResponse<Object>> handleDuplicateLoanRequestException(DuplicateLoanRequestException ex) {
        log.error("Duplicate loan request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(BaseResponse.error("DUPLICATE_LOAN_REQUEST"));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.error("Validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error("VALIDATION_FAILED"));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error("INVALID_ARGUMENT"));
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResponse<Object>> handleIllegalStateException(IllegalStateException ex) {
        log.error("Invalid state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error("INVALID_STATE"));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
    }
}