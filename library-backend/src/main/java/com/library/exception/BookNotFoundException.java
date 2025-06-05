package com.library.exception;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String message) {
        super(message);
    }
    
    public BookNotFoundException(Long id) {
        super("Book not found with id: " + id);
    }
    
    public BookNotFoundException(String field, String value) {
        super("Book not found with " + field + ": " + value);
    }
}