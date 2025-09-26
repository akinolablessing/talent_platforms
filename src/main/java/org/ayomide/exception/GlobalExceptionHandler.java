package org.ayomide.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handle(ApiException exception) {
        return ResponseEntity.status(exception.getStatus()).body(exception.getMessage());
    }

}
