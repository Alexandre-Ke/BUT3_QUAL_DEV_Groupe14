package com.iut.banque.api.error;

import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.exceptions.InsufficientFundsException;
import com.iut.banque.exceptions.TechnicalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

class LegacyGlobalExceptionHandler {

  @ExceptionHandler({ IllegalFormatException.class, IllegalOperationException.class })
  public ResponseEntity<Map<String, Object>> badRequest(Exception ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex));
  }

  @ExceptionHandler(InsufficientFundsException.class)
  public ResponseEntity<Map<String, Object>> forbidden(Exception ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(ex));
  }

  @ExceptionHandler(TechnicalException.class)
  public ResponseEntity<Map<String, Object>> conflict(Exception ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body(ex));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> internal(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body(ex));
  }

  private Map<String, Object> body(Exception ex) {
    return Map.of(
        "timestamp", Instant.now().toString(),
        "error", ex.getClass().getSimpleName(),
        "message", ex.getMessage());
  }
}
