package com.e.bidding.item_service.exeption;

import com.e.bidding.item_service.dto.ResponseDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseDTO<Object>> handleDuplicateKey(DataIntegrityViolationException ex) {
        String message = ex.getRootCause() != null
                ? ex.getRootCause().getMessage()
                : ex.getMessage(); // fallback message

        return ResponseEntity.badRequest().body(
                new ResponseDTO<>(false, null, "Duplicate key: " + message)
        );
    }


    @ExceptionHandler(UnexpectedRollbackException.class)
    public ResponseEntity<ResponseDTO<Object>> handleRollback(UnexpectedRollbackException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ResponseDTO<>(false, null, "Transaction rolled back: " + ex.getMessage())
        );
    }

    // Optional: general fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Object>> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseDTO<>(false, null, "Error: " + ex.getMessage())
        );
    }
}

