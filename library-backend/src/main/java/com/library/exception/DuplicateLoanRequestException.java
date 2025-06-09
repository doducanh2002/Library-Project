package com.library.exception;

public class DuplicateLoanRequestException extends RuntimeException {
    public DuplicateLoanRequestException(String message) {
        super(message);
    }
}