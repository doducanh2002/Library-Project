package com.library.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(Long bookId, int requested, int available) {
        super("Insufficient stock for book ID: " + bookId + 
              ". Requested: " + requested + ", Available: " + available);
    }
}