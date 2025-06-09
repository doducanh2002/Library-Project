package com.library.exception;

public class MaxLoansExceededException extends RuntimeException {
    public MaxLoansExceededException(String message) {
        super(message);
    }
}