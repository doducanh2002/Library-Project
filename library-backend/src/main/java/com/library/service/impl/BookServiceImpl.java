package com.library.service.impl;

import com.library.dto.*;
import com.library.entity.*;
import com.library.exception.*;
import com.library.mapper.BookMapper;
import com.library.repository.*;
import com.library.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {
    
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final BookAuthorRepository bookAuthorRepository;
    private final BookMapper bookMapper;
    
    @Override
    public BookDetailDTO createBook(CreateBookRequestDTO request) {
        log.info("Creating new book with title: {}", request.getTitle());
        
        validateBookData(request);
        checkIsbnUniqueness(request.getIsbn(), null);
        
        Book book = bookMapper.toEntity(request);
        
        // Set related entities
        setBookRelatedEntities(book, request.getCategoryId(), request.getPublisherId());
        
        // Save book first
        Book savedBook = bookRepository.save(book);
        
        // Handle authors relationship
        createBookAuthorRelationships(savedBook, request.getAuthorIds());
        
        // Reload book with all relationships
        Book bookWithRelations = bookRepository.findById(savedBook.getId())
            .orElseThrow(() -> new BookNotFoundException(savedBook.getId()));
        
        log.info("Book created successfully with ID: {}", bookWithRelations.getId());
        return bookMapper.toDetailDTO(bookWithRelations);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BookDetailDTO getBookById(Long id) {
        log.debug("Fetching book by ID: {}", id);
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
        return bookMapper.toDetailDTO(book);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BookDetailDTO getBookByIsbn(String isbn) {
        log.debug("Fetching book by ISBN: {}", isbn);
        Book book = bookRepository.findByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException("ISBN", isbn));
        return bookMapper.toDetailDTO(book);
    }
    
    @Override
    public BookDetailDTO updateBook(Long id, UpdateBookRequestDTO request) {
        log.info("Updating book with ID: {}", id);
        
        Book existingBook = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
        
        validateBookData(request);
        checkIsbnUniqueness(request.getIsbn(), id);
        
        // Update book fields
        bookMapper.updateEntityFromDTO(request, existingBook);
        
        // Update related entities
        setBookRelatedEntities(existingBook, request.getCategoryId(), request.getPublisherId());
        
        // Update authors relationship
        updateBookAuthorRelationships(existingBook, request.getAuthorIds());
        
        Book updatedBook = bookRepository.save(existingBook);
        
        log.info("Book updated successfully with ID: {}", updatedBook.getId());
        return bookMapper.toDetailDTO(updatedBook);
    }
    
    @Override
    public void deleteBook(Long id) {
        log.info("Deleting book with ID: {}", id);
        
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
        
        // Check if book can be deleted (no active loans or orders)
        validateBookDeletion(book);
        
        bookRepository.delete(book);
        log.info("Book deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> searchBooks(String title, String author, Long categoryId, 
                                   Boolean isLendable, Boolean isSellable, 
                                   String language, Pageable pageable) {
        log.debug("Searching books with filters - title: {}, author: {}, categoryId: {}", 
                 title, author, categoryId);
        
        Specification<Book> spec = buildBookSearchSpecification(title, author, categoryId, 
                                                               isLendable, isSellable, language);
        
        Page<Book> books = bookRepository.findAll(spec, pageable);
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        log.debug("Fetching all books with pagination");
        Page<Book> books = bookRepository.findAll(pageable);
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> getPopularBooks(int limit) {
        log.debug("Fetching popular books with limit: {}", limit);
        List<Book> books = bookRepository.findPopularBooks(Pageable.ofSize(limit));
        return bookMapper.toDTOList(books);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> getBooksByCategory(Long categoryId) {
        log.debug("Fetching books by category ID: {}", categoryId);
        List<Book> books = bookRepository.findByCategoryId(categoryId);
        return bookMapper.toDTOList(books);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> getBooksByAuthor(Long authorId) {
        log.debug("Fetching books by author ID: {}", authorId);
        List<Book> books = bookRepository.findByBookAuthors_AuthorId(authorId);
        return bookMapper.toDTOList(books);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> getBooksByPublisher(Long publisherId) {
        log.debug("Fetching books by publisher ID: {}", publisherId);
        List<Book> books = bookRepository.findByPublisherId(publisherId);
        return bookMapper.toDTOList(books);
    }
    
    @Override
    public void updateStock(Long bookId, int newStockForSale) {
        log.info("Updating stock for book ID: {} to {}", bookId, newStockForSale);
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        validateStockOperation(bookId, newStockForSale, "UPDATE_STOCK");
        
        book.setStockForSale(newStockForSale);
        bookRepository.save(book);
        
        log.info("Stock updated successfully for book ID: {}", bookId);
    }
    
    @Override
    public void updateLoanCopies(Long bookId, int totalCopies, int availableCopies) {
        log.info("Updating loan copies for book ID: {} - total: {}, available: {}", 
                bookId, totalCopies, availableCopies);
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        if (availableCopies > totalCopies) {
            throw new IllegalArgumentException("Available copies cannot exceed total copies");
        }
        
        if (availableCopies < 0 || totalCopies < 0) {
            throw new IllegalArgumentException("Copies cannot be negative");
        }
        
        book.setTotalCopiesForLoan(totalCopies);
        book.setAvailableCopiesForLoan(availableCopies);
        bookRepository.save(book);
        
        log.info("Loan copies updated successfully for book ID: {}", bookId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isAvailableForLoan(Long bookId) {
        return getAvailableCopiesForLoan(bookId) > 0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isAvailableForSale(Long bookId, int quantity) {
        return getStockForSale(bookId) >= quantity;
    }
    
    @Override
    public void reserveForLoan(Long bookId) {
        log.info("Reserving book for loan: {}", bookId);
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        if (!canLendBook(bookId)) {
            throw new InsufficientStockException("No copies available for loan");
        }
        
        book.setAvailableCopiesForLoan(book.getAvailableCopiesForLoan() - 1);
        bookRepository.save(book);
        
        log.info("Book reserved for loan successfully: {}", bookId);
    }
    
    @Override
    public void releaseFromLoan(Long bookId) {
        log.info("Releasing book from loan: {}", bookId);
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        int newAvailable = book.getAvailableCopiesForLoan() + 1;
        if (newAvailable > book.getTotalCopiesForLoan()) {
            throw new IllegalStateException("Cannot release more copies than total available");
        }
        
        book.setAvailableCopiesForLoan(newAvailable);
        bookRepository.save(book);
        
        log.info("Book released from loan successfully: {}", bookId);
    }
    
    @Override
    public void reserveForSale(Long bookId, int quantity) {
        log.info("Reserving {} units for sale: {}", quantity, bookId);
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        if (!canSellBook(bookId, quantity)) {
            throw new InsufficientStockException(bookId, quantity, book.getStockForSale());
        }
        
        book.setStockForSale(book.getStockForSale() - quantity);
        bookRepository.save(book);
        
        log.info("Book reserved for sale successfully: {} units of book {}", quantity, bookId);
    }
    
    @Override
    public void releaseFromSale(Long bookId, int quantity) {
        log.info("Releasing {} units from sale: {}", quantity, bookId);
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        book.setStockForSale(book.getStockForSale() + quantity);
        bookRepository.save(book);
        
        log.info("Book released from sale successfully: {} units of book {}", quantity, bookId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canLendBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        return book.getIsLendable() && book.getAvailableCopiesForLoan() > 0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canSellBook(Long bookId, int quantity) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        return book.getIsSellable() && book.getStockForSale() >= quantity;
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getAvailableCopiesForLoan(Long bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        return book.getAvailableCopiesForLoan();
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getStockForSale(Long bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        return book.getStockForSale();
    }
    
    @Override
    public void validateBookData(CreateBookRequestDTO request) {
        validateCommonBookData(request.getTitle(), request.getIsbn(), 
                             request.getCategoryId(), request.getPublisherId(), 
                             request.getAuthorIds());
    }
    
    @Override
    public void validateBookData(UpdateBookRequestDTO request) {
        validateCommonBookData(request.getTitle(), request.getIsbn(), 
                             request.getCategoryId(), request.getPublisherId(), 
                             request.getAuthorIds());
    }
    
    @Override
    public void validateStockOperation(Long bookId, int quantity, String operation) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        switch (operation) {
            case "RESERVE_SALE":
                if (quantity > book.getStockForSale()) {
                    throw new InsufficientStockException(bookId, quantity, book.getStockForSale());
                }
                break;
            case "RESERVE_LOAN":
                if (book.getAvailableCopiesForLoan() <= 0) {
                    throw new InsufficientStockException("No copies available for loan");
                }
                break;
        }
    }
    
    // Private helper methods
    
    private void validateCommonBookData(String title, String isbn, Long categoryId, 
                                      Long publisherId, List<Long> authorIds) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN is required");
        }
        
        if (categoryId == null) {
            throw new IllegalArgumentException("Category is required");
        }
        
        if (publisherId == null) {
            throw new IllegalArgumentException("Publisher is required");
        }
        
        if (authorIds == null || authorIds.isEmpty()) {
            throw new IllegalArgumentException("At least one author is required");
        }
        
        // Validate related entities exist
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found with ID: " + categoryId);
        }
        
        if (!publisherRepository.existsById(publisherId)) {
            throw new IllegalArgumentException("Publisher not found with ID: " + publisherId);
        }
        
        for (Long authorId : authorIds) {
            if (!authorRepository.existsById(authorId)) {
                throw new IllegalArgumentException("Author not found with ID: " + authorId);
            }
        }
    }
    
    private void checkIsbnUniqueness(String isbn, Long excludeBookId) {
        boolean exists = excludeBookId == null ? 
            bookRepository.existsByIsbn(isbn) : 
            bookRepository.existsByIsbnAndIdNot(isbn, excludeBookId);
            
        if (exists) {
            throw DuplicateBookException.forIsbn(isbn);
        }
    }
    
    private void setBookRelatedEntities(Book book, Long categoryId, Long publisherId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        Publisher publisher = publisherRepository.findById(publisherId)
            .orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
        
        book.setCategory(category);
        book.setPublisher(publisher);
    }
    
    private void createBookAuthorRelationships(Book book, List<Long> authorIds) {
        for (Long authorId : authorIds) {
            Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found with ID: " + authorId));
            
            BookAuthor bookAuthor = new BookAuthor();
            bookAuthor.setBook(book);
            bookAuthor.setAuthor(author);
            bookAuthor.setAuthorRole("AUTHOR");
            
            bookAuthorRepository.save(bookAuthor);
        }
    }
    
    private void updateBookAuthorRelationships(Book book, List<Long> newAuthorIds) {
        // Remove existing relationships
        bookAuthorRepository.deleteByBookId(book.getId());
        
        // Create new relationships
        createBookAuthorRelationships(book, newAuthorIds);
    }
    
    private void validateBookDeletion(Book book) {
        // Add validation logic for checking active loans, orders, etc.
        // This will be implemented when loan and order entities are available
    }
    
    private Specification<Book> buildBookSearchSpecification(String title, String author, 
                                                           Long categoryId, Boolean isLendable, 
                                                           Boolean isSellable, String language) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), 
                    "%" + title.toLowerCase() + "%"
                ));
            }
            
            if (author != null && !author.trim().isEmpty()) {
                var authorJoin = root.join("bookAuthors").join("author");
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(authorJoin.get("name")), 
                    "%" + author.toLowerCase() + "%"
                ));
            }
            
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            
            if (isLendable != null) {
                predicates.add(criteriaBuilder.equal(root.get("isLendable"), isLendable));
            }
            
            if (isSellable != null) {
                predicates.add(criteriaBuilder.equal(root.get("isSellable"), isSellable));
            }
            
            if (language != null && !language.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("language"), language));
            }
            
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}