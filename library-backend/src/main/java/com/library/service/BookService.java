package com.library.service;

import com.library.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    
    // CRUD Operations
    BookDetailDTO createBook(CreateBookRequestDTO request);
    
    BookDetailDTO getBookById(Long id);
    
    BookDetailDTO getBookByIsbn(String isbn);
    
    BookDetailDTO updateBook(Long id, UpdateBookRequestDTO request);
    
    void deleteBook(Long id);
    
    // Search and List Operations
    Page<BookDTO> searchBooks(String title, String author, Long categoryId, 
                             Boolean isLendable, Boolean isSellable, 
                             String language, Pageable pageable);
    
    Page<BookDTO> searchBooksWithCriteria(BookSearchCriteria criteria, Pageable pageable);
    
    Page<BookDTO> getAllBooks(Pageable pageable);
    
    List<BookDTO> getPopularBooks(int limit);
    
    List<BookDTO> getBooksByCategory(Long categoryId);
    
    List<BookDTO> getBooksByAuthor(Long authorId);
    
    List<BookDTO> getBooksByPublisher(Long publisherId);
    
    // Stock Management
    void updateStock(Long bookId, int newStockForSale);
    
    void updateLoanCopies(Long bookId, int totalCopies, int availableCopies);
    
    boolean isAvailableForLoan(Long bookId);
    
    boolean isAvailableForSale(Long bookId, int quantity);
    
    void reserveForLoan(Long bookId);
    
    void releaseFromLoan(Long bookId);
    
    void reserveForSale(Long bookId, int quantity);
    
    void releaseFromSale(Long bookId, int quantity);
    
    // Business Logic
    boolean canLendBook(Long bookId);
    
    boolean canSellBook(Long bookId, int quantity);
    
    int getAvailableCopiesForLoan(Long bookId);
    
    int getStockForSale(Long bookId);
    
    // Validation
    void validateBookData(CreateBookRequestDTO request);
    
    void validateBookData(UpdateBookRequestDTO request);
    
    void validateStockOperation(Long bookId, int quantity, String operation);
}