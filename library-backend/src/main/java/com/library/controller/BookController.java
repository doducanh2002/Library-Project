package com.library.controller;

import com.library.dto.*;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Books", description = "Book management APIs")
public class BookController {
    
    private final BookService bookService;
    
    // Public endpoints
    
    @GetMapping("/books")
    @Operation(summary = "Search and list books", description = "Get paginated list of books with optional search filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<com.library.dto.ApiResponse<Page<BookDTO>>> searchBooks(
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
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BookDTO> books = bookService.searchBooks(title, author, categoryId, isLendable, isSellable, language, pageable);
        
        return ResponseEntity.ok(com.library.dto.ApiResponse.success("Books retrieved successfully", books));
    }
    
    @GetMapping("/books/{id}")
    @Operation(summary = "Get book by ID", description = "Get detailed information about a specific book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book found"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<com.library.dto.ApiResponse<BookDetailDTO>> getBookById(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        
        BookDetailDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(com.library.dto.ApiResponse.success("Book retrieved successfully", book));
    }
    
    @GetMapping("/books/isbn/{isbn}")
    @Operation(summary = "Get book by ISBN", description = "Get detailed information about a book by its ISBN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book found"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<com.library.dto.ApiResponse<BookDetailDTO>> getBookByIsbn(
            @Parameter(description = "Book ISBN") @PathVariable String isbn) {
        
        BookDetailDTO book = bookService.getBookByIsbn(isbn);
        return ResponseEntity.ok(com.library.dto.ApiResponse.success("Book retrieved successfully", book));
    }
    
    @GetMapping("/books/popular")
    @Operation(summary = "Get popular books", description = "Get list of most popular books")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Popular books retrieved successfully")
    })
    public ResponseEntity<com.library.dto.ApiResponse<List<BookDTO>>> getPopularBooks(
            @Parameter(description = "Number of books to return") @RequestParam(defaultValue = "10") int limit) {
        
        List<BookDTO> books = bookService.getPopularBooks(limit);
        return ResponseEntity.ok(com.library.dto.ApiResponse.success("Popular books retrieved successfully", books));
    }
    
    @GetMapping("/books/category/{categoryId}")
    @Operation(summary = "Get books by category", description = "Get all books in a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<com.library.dto.ApiResponse<List<BookDTO>>> getBooksByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        
        List<BookDTO> books = bookService.getBooksByCategory(categoryId);
        return ResponseEntity.ok(com.library.dto.ApiResponse.success("Books retrieved successfully", books));
    }
    
    @GetMapping("/books/author/{authorId}")
    @Operation(summary = "Get books by author", description = "Get all books by a specific author")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    public ResponseEntity<com.library.dto.ApiResponse<List<BookDTO>>> getBooksByAuthor(
            @Parameter(description = "Author ID") @PathVariable Long authorId) {
        
        List<BookDTO> books = bookService.getBooksByAuthor(authorId);
        return ResponseEntity.ok(com.library.dto.ApiResponse.success("Books retrieved successfully", books));
    }
    
    @GetMapping("/books/publisher/{publisherId}")
    @Operation(summary = "Get books by publisher", description = "Get all books by a specific publisher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Publisher not found")
    })
    public ResponseEntity<com.library.dto.ApiResponse<List<BookDTO>>> getBooksByPublisher(
            @Parameter(description = "Publisher ID") @PathVariable Long publisherId) {
        
        List<BookDTO> books = bookService.getBooksByPublisher(publisherId);
        return ResponseEntity.ok(com.library.dto.ApiResponse.success("Books retrieved successfully", books));
    }
    
    // Admin endpoints - require LIBRARIAN role
    
    @PostMapping("/admin/books")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Create a new book", description = "Create a new book (LIBRARIAN role required)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid book data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Book with this ISBN already exists")
    })
    public ResponseEntity<com.library.dto.ApiResponse<BookDetailDTO>> createBook(
            @Valid @RequestBody CreateBookRequestDTO request) {
        
        log.info("Creating new book with title: {}", request.getTitle());
        BookDetailDTO createdBook = bookService.createBook(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.library.dto.ApiResponse.success("Book created successfully", createdBook));
    }
    
    @PutMapping("/admin/books/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update a book", description = "Update an existing book (LIBRARIAN role required)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid book data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "409", description = "Book with this ISBN already exists")
    })
    public ResponseEntity<com.library.dto.ApiResponse<BookDetailDTO>> updateBook(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequestDTO request) {
        
        log.info("Updating book with ID: {}", id);
        BookDetailDTO updatedBook = bookService.updateBook(id, request);
        
        return ResponseEntity.ok(com.library.dto.ApiResponse.success("Book updated successfully", updatedBook));
    }
    
    @DeleteMapping("/admin/books/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Delete a book", description = "Delete an existing book (LIBRARIAN role required)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "400", description = "Cannot delete book with active loans/orders")
    })
    public ResponseEntity<com.library.dto.ApiResponse<Void>> deleteBook(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        
        log.info("Deleting book with ID: {}", id);
        bookService.deleteBook(id);
        
        return ResponseEntity.ok(com.library.dto.ApiResponse.success("Book deleted successfully", null));
    }
    
    @GetMapping("/admin/books")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get all books for admin", description = "Get paginated list of all books for admin management (LIBRARIAN role required)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<com.library.dto.ApiResponse<Page<BookDTO>>> getAllBooksForAdmin(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BookDTO> books = bookService.getAllBooks(pageable);
        
        return ResponseEntity.ok(com.library.dto.ApiResponse.success("Books retrieved successfully", books));
    }
    
    // Stock management endpoints
    
    @PutMapping("/admin/books/{id}/stock")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update book stock", description = "Update stock quantity for sale (LIBRARIAN role required)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock updated successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "400", description = "Invalid stock quantity")
    })
    public ResponseEntity<com.library.dto.ApiResponse<Void>> updateBookStock(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Parameter(description = "New stock quantity") @RequestParam int stock) {
        
        log.info("Updating stock for book ID: {} to {}", id, stock);
        bookService.updateStock(id, stock);
        
        return ResponseEntity.ok(com.library.dto.ApiResponse.success("Stock updated successfully", null));
    }
    
    @PutMapping("/admin/books/{id}/loan-copies")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update loan copies", description = "Update total and available copies for loan (LIBRARIAN role required)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan copies updated successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "400", description = "Invalid copy quantities")
    })
    public ResponseEntity<com.library.dto.ApiResponse<Void>> updateLoanCopies(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Parameter(description = "Total copies for loan") @RequestParam int totalCopies,
            @Parameter(description = "Available copies for loan") @RequestParam int availableCopies) {
        
        log.info("Updating loan copies for book ID: {} - total: {}, available: {}", id, totalCopies, availableCopies);
        bookService.updateLoanCopies(id, totalCopies, availableCopies);
        
        return ResponseEntity.ok(com.library.dto.ApiResponse.success("Loan copies updated successfully", null));
    }
}