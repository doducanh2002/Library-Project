package com.library.exception;

public class DuplicateBookException extends RuntimeException {
    public DuplicateBookException(String message) {
        super(message);
    }
    
    public DuplicateBookException(String isbn) {
        super("Book with ISBN " + isbn + " already exists");
    }
}