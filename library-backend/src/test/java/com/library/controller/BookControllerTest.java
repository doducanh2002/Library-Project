package com.library.controller;

import com.library.dto.BookDTO;
import com.library.dto.BookDetailDTO;
import com.library.dto.BaseResponse;
import com.library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private BookDTO bookDTO;
    private BookDetailDTO bookDetailDTO;
    private List<BookDTO> bookList;

    @BeforeEach
    void setUp() {
        // Setup test data
        bookDTO = new BookDTO();
        bookDTO.setId(1L);
        bookDTO.setTitle("Test Book");
        bookDTO.setIsbn("978-3-16-148410-0");
        bookDTO.setPrice(BigDecimal.valueOf(29.99));
        bookDTO.setIsLendable(true);
        bookDTO.setIsSellable(true);
        bookDTO.setAvailableCopiesForLoan(5);
        bookDTO.setStockForSale(10);

        bookDetailDTO = new BookDetailDTO();
        bookDetailDTO.setId(1L);
        bookDetailDTO.setTitle("Test Book");
        bookDetailDTO.setIsbn("978-3-16-148410-0");
        bookDetailDTO.setDescription("A test book description");
        bookDetailDTO.setPrice(BigDecimal.valueOf(29.99));
        bookDetailDTO.setIsLendable(true);
        bookDetailDTO.setIsSellable(true);
        bookDetailDTO.setLanguage("en");
        bookDetailDTO.setNumberOfPages(300);
        bookDetailDTO.setPublicationYear(2023);
        bookDetailDTO.setCreatedAt(LocalDateTime.now());
        bookDetailDTO.setUpdatedAt(LocalDateTime.now());

        bookList = Arrays.asList(bookDTO);
    }

    @Test
    void searchBooks_WithParameters_ShouldReturnPagedResults() {
        // Given
        String title = "Test";
        String author = "John Doe";
        Long categoryId = 1L;
        Boolean isLendable = true;
        Boolean isSellable = true;
        String language = "en";
        int page = 0;
        int size = 20;
        String sortBy = "title";
        String sortDir = "asc";

        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> expectedPage = new PageImpl<>(bookList, pageable, 1);

        when(bookService.searchBooks(anyString(), anyString(), anyLong(), anyBoolean(), 
                anyBoolean(), anyString(), any(Pageable.class))).thenReturn(expectedPage);

        // When
        BaseResponse<Page<BookDTO>> response = bookController.searchBooks(
                title, author, categoryId, isLendable, isSellable, language, 
                page, size, sortBy, sortDir);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getTotalElements());
        assertEquals(1, response.getData().getContent().size());

        verify(bookService, times(1)).searchBooks(anyString(), anyString(), anyLong(), 
                anyBoolean(), anyBoolean(), anyString(), any(Pageable.class));
    }

    @Test
    void searchBooks_WithoutParameters_ShouldReturnAllBooks() {
        // Given
        int page = 0;
        int size = 20;
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> expectedPage = new PageImpl<>(bookList, pageable, 1);

        when(bookService.searchBooks(isNull(), isNull(), isNull(), isNull(), 
                isNull(), isNull(), any(Pageable.class))).thenReturn(expectedPage);

        // When
        BaseResponse<Page<BookDTO>> response = bookController.searchBooks(
                null, null, null, null, null, null, page, size, "title", "asc");

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());

        verify(bookService, times(1)).searchBooks(isNull(), isNull(), isNull(), 
                isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getBookById_ExistingBook_ShouldReturnBook() {
        // Given
        Long bookId = 1L;
        when(bookService.getBookById(bookId)).thenReturn(bookDetailDTO);

        // When
        BaseResponse<BookDetailDTO> response = bookController.getBookById(bookId);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(bookId, response.getData().getId());
        assertEquals("Test Book", response.getData().getTitle());

        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    void getBookByIsbn_ExistingBook_ShouldReturnBook() {
        // Given
        String isbn = "978-3-16-148410-0";
        when(bookService.getBookByIsbn(isbn)).thenReturn(bookDetailDTO);

        // When
        BaseResponse<BookDetailDTO> response = bookController.getBookByIsbn(isbn);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(isbn, response.getData().getIsbn());

        verify(bookService, times(1)).getBookByIsbn(isbn);
    }

    @Test
    void getPopularBooks_ShouldReturnLimitedList() {
        // Given
        int limit = 10;
        when(bookService.getPopularBooks(limit)).thenReturn(bookList);

        // When
        BaseResponse<List<BookDTO>> response = bookController.getPopularBooks(limit);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());

        verify(bookService, times(1)).getPopularBooks(limit);
    }

    @Test
    void getPopularBooks_WithDefaultLimit_ShouldReturn10Books() {
        // Given
        int defaultLimit = 10;
        List<BookDTO> tenBooks = Arrays.asList(
                bookDTO, bookDTO, bookDTO, bookDTO, bookDTO,
                bookDTO, bookDTO, bookDTO, bookDTO, bookDTO
        );
        when(bookService.getPopularBooks(defaultLimit)).thenReturn(tenBooks);

        // When
        BaseResponse<List<BookDTO>> response = bookController.getPopularBooks(defaultLimit);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertEquals(10, response.getData().size());

        verify(bookService, times(1)).getPopularBooks(defaultLimit);
    }

    @Test
    void getBooksByCategory_ShouldReturnBooksList() {
        // Given
        Long categoryId = 1L;
        when(bookService.getBooksByCategory(categoryId)).thenReturn(bookList);

        // When
        BaseResponse<List<BookDTO>> response = bookController.getBooksByCategory(categoryId);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertFalse(response.getData().isEmpty());

        verify(bookService, times(1)).getBooksByCategory(categoryId);
    }

    @Test
    void getBooksByAuthor_ShouldReturnBooksList() {
        // Given
        Long authorId = 1L;
        when(bookService.getBooksByAuthor(authorId)).thenReturn(bookList);

        // When
        BaseResponse<List<BookDTO>> response = bookController.getBooksByAuthor(authorId);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertFalse(response.getData().isEmpty());

        verify(bookService, times(1)).getBooksByAuthor(authorId);
    }

    @Test
    void getBooksByPublisher_ShouldReturnBooksList() {
        // Given
        Long publisherId = 1L;
        when(bookService.getBooksByPublisher(publisherId)).thenReturn(bookList);

        // When
        BaseResponse<List<BookDTO>> response = bookController.getBooksByPublisher(publisherId);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertNotNull(response.getData());
        assertFalse(response.getData().isEmpty());

        verify(bookService, times(1)).getBooksByPublisher(publisherId);
    }
}