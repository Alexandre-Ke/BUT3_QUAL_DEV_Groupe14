package com.iut.banque.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.iut.banque.exceptions.IllegalFormatException;
import com.iut.banque.exceptions.IllegalOperationException;
import com.iut.banque.exceptions.InsufficientFundsException;
import com.iut.banque.exceptions.TechnicalException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalOperationException.class)
  public ProblemDetail illegalOperation(IllegalOperationException ex) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Illegal operation");
    pd.setDetail(ex.getMessage());
    return pd;
  }

  @ExceptionHandler(IllegalFormatException.class)
  public ProblemDetail illegalFormat(IllegalFormatException ex) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Illegal format");
    pd.setDetail(ex.getMessage());
    return pd;
  }

  @ExceptionHandler(InsufficientFundsException.class)
  public ProblemDetail insufficient(InsufficientFundsException ex) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Insufficient funds");
    pd.setDetail(ex.getMessage());
    return pd;
  }

  @ExceptionHandler(TechnicalException.class)
  public ProblemDetail technical(TechnicalException ex) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    pd.setTitle("Technical error");
    pd.setDetail(ex.getMessage());
    return pd;
  }
}
