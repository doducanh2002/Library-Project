package com.library.controller;

import com.library.dto.*;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@Tag(name = "Books", description = "Book management APIs")
@CrossOrigin(origins = "*")
public class BookController {
    
    private final BookService bookService;
    
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    
    // Public endpoints
    
    @GetMapping("/books")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search and list books", description = "Get paginated list of books with optional search filters")
    public BaseResponse<Page<BookDTO>> searchBooks(
            @Parameter(description = "Book title to search for") @RequestParam(required = false) String title,
            @Parameter(description = "Author name to search for") @RequestParam(required = false) String author,
            @Parameter(description = "Category ID to filter by") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by lendable books") @RequestParam(required = false) Boolean isLendable,
            @Parameter(description = "Filter by sellable books") @RequestParam(required = false) Boolean isSellable,
            @Parameter(description = "Language code to filter by") @RequestParam(required = false) String language,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Received search books request - title: {}, author: {}, categoryId: {}", title, author, categoryId);
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BookDTO> books = bookService.searchBooks(title, author, categoryId, isLendable, isSellable, language, pageable);
        return BaseResponse.success(books);
    }
    
    @GetMapping("/books/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get book by ID", description = "Get detailed information about a specific book")
    public BaseResponse<BookDetailDTO> getBookById(@Parameter(description = "Book ID") @PathVariable Long id) {
        log.info("Received get book by ID request: {}", id);
        BookDetailDTO book = bookService.getBookById(id);
        return BaseResponse.success(book);
    }
    
    @GetMapping("/books/isbn/{isbn}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get book by ISBN", description = "Get detailed information about a book by its ISBN")
    public BaseResponse<BookDetailDTO> getBookByIsbn(@Parameter(description = "Book ISBN") @PathVariable String isbn) {
        log.info("Received get book by ISBN request: {}", isbn);
        BookDetailDTO book = bookService.getBookByIsbn(isbn);
        return BaseResponse.success(book);
    }
    
    @GetMapping("/books/popular")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get popular books", description = "Get list of most popular books")
    public BaseResponse<List<BookDTO>> getPopularBooks(
            @Parameter(description = "Number of books to return") @RequestParam(defaultValue = "10") int limit) {
        log.info("Received get popular books request with limit: {}", limit);
        List<BookDTO> books = bookService.getPopularBooks(limit);
        return BaseResponse.success(books);
    }
    
    @GetMapping("/books/category/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get books by category", description = "Get all books in a specific category")
    public BaseResponse<List<BookDTO>> getBooksByCategory(@Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("Received get books by category request: {}", categoryId);
        List<BookDTO> books = bookService.getBooksByCategory(categoryId);
        return BaseResponse.success(books);
    }
    
    @GetMapping("/books/author/{authorId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get books by author", description = "Get all books by a specific author")
    public BaseResponse<List<BookDTO>> getBooksByAuthor(@Parameter(description = "Author ID") @PathVariable Long authorId) {
        log.info("Received get books by author request: {}", authorId);
        List<BookDTO> books = bookService.getBooksByAuthor(authorId);
        return BaseResponse.success(books);
    }
    
    @GetMapping("/books/publisher/{publisherId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get books by publisher", description = "Get all books by a specific publisher")
    public BaseResponse<List<BookDTO>> getBooksByPublisher(@Parameter(description = "Publisher ID") @PathVariable Long publisherId) {
        log.info("Received get books by publisher request: {}", publisherId);
        List<BookDTO> books = bookService.getBooksByPublisher(publisherId);
        return BaseResponse.success(books);
    }
    
    // Admin endpoints - require LIBRARIAN role
    
    @PostMapping("/admin/books")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Create a new book", description = "Create a new book (LIBRARIAN role required)")
    public BaseResponse<BookDetailDTO> createBook(@RequestBody @Validated CreateBookRequestDTO request) {
        log.info("Received create book request for title: {}", request.getTitle());
        BookDetailDTO createdBook = bookService.createBook(request);
        return BaseResponse.success(createdBook);
    }
    
    @PutMapping("/admin/books/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update a book", description = "Update an existing book (LIBRARIAN role required)")
    public BaseResponse<BookDetailDTO> updateBook(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @RequestBody @Validated UpdateBookRequestDTO request) {
        log.info("Received update book request for ID: {}", id);
        BookDetailDTO updatedBook = bookService.updateBook(id, request);
        return BaseResponse.success(updatedBook);
    }
    
    @DeleteMapping("/admin/books/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Delete a book", description = "Delete an existing book (LIBRARIAN role required)")
    public BaseResponse<String> deleteBook(@Parameter(description = "Book ID") @PathVariable Long id) {
        log.info("Received delete book request for ID: {}", id);
        bookService.deleteBook(id);
        return BaseResponse.success("Book deleted successfully");
    }
    
    @GetMapping("/admin/books")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get all books for admin", description = "Get paginated list of all books for admin management (LIBRARIAN role required)")
    public BaseResponse<Page<BookDTO>> getAllBooksForAdmin(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Received get all books for admin request");
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BookDTO> books = bookService.getAllBooks(pageable);
        return BaseResponse.success(books);
    }
    
    // Stock management endpoints
    
    @PutMapping("/admin/books/{id}/stock")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update book stock", description = "Update stock quantity for sale (LIBRARIAN role required)")
    public BaseResponse<String> updateBookStock(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Parameter(description = "New stock quantity") @RequestParam int stock) {
        log.info("Received update stock request for book ID: {} to {}", id, stock);
        bookService.updateStock(id, stock);
        return BaseResponse.success("Stock updated successfully");
    }
    
    @PutMapping("/admin/books/{id}/loan-copies")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update loan copies", description = "Update total and available copies for loan (LIBRARIAN role required)")
    public BaseResponse<String> updateLoanCopies(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Parameter(description = "Total copies for loan") @RequestParam int totalCopies,
            @Parameter(description = "Available copies for loan") @RequestParam int availableCopies) {
        log.info("Received update loan copies request for book ID: {} - total: {}, available: {}", id, totalCopies, availableCopies);
        bookService.updateLoanCopies(id, totalCopies, availableCopies);
        return BaseResponse.success("Loan copies updated successfully");
    }
}