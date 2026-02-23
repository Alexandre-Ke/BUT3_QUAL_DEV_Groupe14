package com.iut.banque.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsufficientFundsExceptionTest {

    @Test
    void create_exception_with_message() {
        String message = "Insufficient funds in account";
        InsufficientFundsException exception = new InsufficientFundsException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void create_exception_with_message_and_cause() {
        String message = "Insufficient funds";
        Throwable cause = new RuntimeException("Account check failed");
        InsufficientFundsException exception = new InsufficientFundsException(message, cause);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void exception_extends_exception() {
        InsufficientFundsException exception = new InsufficientFundsException("Test");

        assertTrue(exception instanceof Exception);
    }

    @Test
    void exception_with_numeric_context() {
        String message = "Insufficient funds: required 1000.00, available 500.00";
        InsufficientFundsException exception = new InsufficientFundsException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void exception_stack_trace_available() {
        InsufficientFundsException exception = new InsufficientFundsException("Test");

        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }
}
