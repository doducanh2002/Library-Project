package com.library.exception;

public class DuplicateBookException extends RuntimeException {
    public DuplicateBookException(String message) {
        super(message);
    }
    
    public static DuplicateBookException forIsbn(String isbn) {
        return new DuplicateBookException("Book with ISBN " + isbn + " already exists");
    }
}