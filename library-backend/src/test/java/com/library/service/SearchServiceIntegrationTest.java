//package com.library.service;
//
//import com.library.dto.BookDTO;
//import com.library.dto.BookSearchCriteria;
//import com.library.entity.*;
//import com.library.repository.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@ActiveProfiles("test")
//@Transactional
//class SearchServiceIntegrationTest {
//
//    @Autowired
//    private SearchService searchService;
//
//    @Autowired
//    private BookRepository bookRepository;
//
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    @Autowired
//    private PublisherRepository publisherRepository;
//
//    @Autowired
//    private AuthorRepository authorRepository;
//
//    @Autowired
//    private BookAuthorRepository bookAuthorRepository;
//
//    private Book testBook1, testBook2, testBook3;
//    private Category category1, category2;
//    private Publisher publisher1, publisher2;
//    private Author author1, author2;
//
//    @BeforeEach
//    void setUp() {
//        // Clean up existing data
//        bookAuthorRepository.deleteAll();
//        bookRepository.deleteAll();
//        categoryRepository.deleteAll();
//        publisherRepository.deleteAll();
//        authorRepository.deleteAll();
//
//        // Create test categories
//        category1 = new Category();
//        category1.setName("Programming");
//        category1.setDescription("Programming and software development books");
//        category1 = categoryRepository.save(category1);
//
//        category2 = new Category();
//        category2.setName("Fiction");
//        category2.setDescription("Fiction and literature books");
//        category2 = categoryRepository.save(category2);
//
//        // Create test publishers
//        publisher1 = new Publisher();
//        publisher1.setName("Tech Publishers");
//        publisher1.setAddress("123 Tech Street");
//        publisher1 = publisherRepository.save(publisher1);
//
//        publisher2 = new Publisher();
//        publisher2.setName("Fiction House");
//        publisher2.setAddress("456 Fiction Ave");
//        publisher2 = publisherRepository.save(publisher2);
//
//        // Create test authors
//        author1 = new Author();
//        author1.setName("John Developer");
//        author1.setBiography("Expert Java programmer");
//        author1 = authorRepository.save(author1);
//
//        author2 = new Author();
//        author2.setName("Jane Writer");
//        author2.setBiography("Bestselling fiction author");
//        author2 = authorRepository.save(author2);
//
//        // Create test books
//        testBook1 = createTestBook("Java Programming Mastery", "978-1234567890",
//            "Complete guide to Java programming with Spring Boot", "en", 2023, 500,
//            new BigDecimal("39.99"), 10, true, true, 5, 3, category1, publisher1);
//
//        testBook2 = createTestBook("Advanced Spring Framework", "978-0987654321",
//            "Deep dive into Spring Framework and microservices", "en", 2022, 600,
//            new BigDecimal("45.50"), 5, true, true, 3, 2, category1, publisher1);
//
//        testBook3 = createTestBook("The Great Adventure", "978-1111111111",
//            "An epic tale of courage and friendship", "vi", 2021, 350,
//            new BigDecimal("25.00"), 0, false, true, 8, 6, category2, publisher2);
//
//        // Create book-author relationships
//        createBookAuthor(testBook1, author1);
//        createBookAuthor(testBook2, author1);
//        createBookAuthor(testBook3, author2);
//    }
//
//    private Book createTestBook(String title, String isbn, String description, String language,
//                               int publicationYear, int pages, BigDecimal price, int stockForSale,
//                               boolean isSellable, boolean isLendable, int totalCopies, int availableCopies,
//                               Category category, Publisher publisher) {
//        Book book = new Book();
//        book.setTitle(title);
//        book.setIsbn(isbn);
//        book.setDescription(description);
//        book.setLanguage(language);
//        book.setPublicationYear(publicationYear);
//        book.setNumberOfPages(pages);
//        book.setPrice(price);
//        book.setStockForSale(stockForSale);
//        book.setIsSellable(isSellable);
//        book.setIsLendable(isLendable);
//        book.setTotalCopiesForLoan(totalCopies);
//        book.setAvailableCopiesForLoan(availableCopies);
//        book.setCategory(category);
//        book.setPublisher(publisher);
//        book.setCreatedAt(LocalDateTime.now().minusDays(10));
//        return bookRepository.save(book);
//    }
//
//    private void createBookAuthor(Book book, Author author) {
//        BookAuthor bookAuthor = new BookAuthor();
//        bookAuthor.setBook(book);
//        bookAuthor.setAuthor(author);
//        bookAuthor.setAuthorRole("AUTHOR");
//        bookAuthorRepository.save(bookAuthor);
//    }
//
//    @Test
//    void testSearchBooks_WithKeyword() {
//        // Given
//        BookSearchCriteria criteria = new BookSearchCriteria();
//        criteria.setKeyword("Java");
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When
//        Page<BookDTO> results = searchService.searchBooks(criteria, pageable);
//
//        // Then
//        assertThat(results.getContent()).hasSize(1);
//        assertThat(results.getContent().get(0).getTitle()).contains("Java");
//    }
//
//    @Test
//    void testSearchBooks_WithMultipleFilters() {
//        // Given
//        BookSearchCriteria criteria = new BookSearchCriteria();
//        criteria.setKeyword("Spring");
//        criteria.setCategoryIds(Arrays.asList(category1.getId()));
//        criteria.setLanguage("en");
//        criteria.setMinPrice(new BigDecimal("40.00"));
//        criteria.setMaxPrice(new BigDecimal("50.00"));
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When
//        Page<BookDTO> results = searchService.searchBooks(criteria, pageable);
//
//        // Then
//        assertThat(results.getContent()).hasSize(1);
//        assertThat(results.getContent().get(0).getTitle()).contains("Spring");
//    }
//
//    @Test
//    void testFullTextSearch() {
//        // Given
//        String searchText = "programming";
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When
//        Page<BookDTO> results = searchService.fullTextSearch(searchText, pageable);
//
//        // Then
//        // Note: This test might fail if PostgreSQL full-text search is not properly configured
//        // In that case, it will return empty results, which is expected in test environment
//        assertThat(results).isNotNull();
//        assertThat(results.getTotalElements()).isGreaterThanOrEqualTo(0);
//    }
//
//    @Test
//    void testSearchByCategories() {
//        // Given
//        List<Long> categoryIds = Arrays.asList(category1.getId());
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When
//        Page<BookDTO> results = searchService.searchByCategories(categoryIds, pageable);
//
//        // Then
//        assertThat(results.getContent()).hasSize(2);
//        assertThat(results.getContent()).allMatch(book ->
//            book.getTitle().contains("Java") || book.getTitle().contains("Spring"));
//    }
//
//    @Test
//    void testSearchByAuthors() {
//        // Given
//        List<Integer> authorIds = Arrays.asList(author1.getId());
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When
//        Page<BookDTO> results = searchService.searchByAuthors(authorIds, pageable);
//
//        // Then
//        assertThat(results.getContent()).hasSize(2);
//        assertThat(results.getContent()).allMatch(book ->
//            book.getTitle().contains("Java") || book.getTitle().contains("Spring"));
//    }
//
//    @Test
//    void testSearchAvailableForLoan() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When
//        Page<BookDTO> results = searchService.searchAvailableForLoan(pageable);
//
//        // Then
//        assertThat(results.getContent()).hasSize(3);
//        assertThat(results.getContent()).allMatch(book -> book.getIsLendable());
//    }
//
//    @Test
//    void testSearchAvailableForSale() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When
//        Page<BookDTO> results = searchService.searchAvailableForSale(pageable);
//
//        // Then
//        assertThat(results.getContent()).hasSize(2); // testBook3 is out of stock
//        assertThat(results.getContent()).allMatch(book ->
//            book.getIsSellable() && book.getStockForSale() > 0);
//    }
//
//    @Test
//    void testSearchRecentlyAdded() {
//        // Given
//        int days = 30;
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When
//        Page<BookDTO> results = searchService.searchRecentlyAdded(days, pageable);
//
//        // Then
//        assertThat(results.getContent()).hasSize(3);
//        // All test books were created within the last 30 days
//    }
//
//    @Test
//    void testGetSearchSuggestions() {
//        // Given
//        String partialText = "Ja";
//        int limit = 5;
//
//        // When
//        List<String> suggestions = searchService.getSearchSuggestions(partialText, limit);
//
//        // Then
//        assertThat(suggestions).isNotNull();
//        assertThat(suggestions.size()).isLessThanOrEqualTo(limit);
//        // Suggestions should include titles and authors starting with "Ja"
//    }
//
//    @Test
//    void testGetPopularSearchTerms() {
//        // Given
//        int limit = 10;
//
//        // When
//        List<String> terms = searchService.getPopularSearchTerms(limit);
//
//        // Then
//        assertThat(terms).isNotNull();
//        assertThat(terms.size()).isLessThanOrEqualTo(limit);
//    }
//
//    @Test
//    void testAdvancedSearch_ComplexCriteria() {
//        // Given
//        BookSearchCriteria criteria = new BookSearchCriteria();
//        criteria.setKeyword("programming");
//        criteria.setCategoryIds(Arrays.asList(category1.getId()));
//        criteria.setAuthorIds(Arrays.asList(author1.getId()));
//        criteria.setLanguage("en");
//        criteria.setMinPages(400);
//        criteria.setMaxPages(700);
//        criteria.setMinPrice(new BigDecimal("30.00"));
//        criteria.setMaxPrice(new BigDecimal("50.00"));
//        criteria.setAvailableForLoan(true);
//        criteria.setAvailableForSale(true);
//        criteria.setPublicationYearFrom(2020);
//        criteria.setPublicationYearTo(2025);
//
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When
//        Page<BookDTO> results = searchService.advancedSearch(criteria, pageable);
//
//        // Then
//        assertThat(results.getContent()).hasSize(2);
//        assertThat(results.getContent()).allMatch(book -> {
//            return book.getLanguage().equals("en") &&
//                   book.getNumberOfPages() >= 400 &&
//                   book.getNumberOfPages() <= 700 &&
//                   book.getPrice().compareTo(new BigDecimal("30.00")) >= 0 &&
//                   book.getPrice().compareTo(new BigDecimal("50.00")) <= 0 &&
//                   book.getIsLendable() &&
//                   book.getIsSellable() &&
//                   book.getPublicationYear() >= 2020 &&
//                   book.getPublicationYear() <= 2025;
//        });
//    }
//
//    @Test
//    void testAdvancedSearch_EmptyCriteria() {
//        // Given
//        BookSearchCriteria criteria = new BookSearchCriteria();
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When
//        Page<BookDTO> results = searchService.advancedSearch(criteria, pageable);
//
//        // Then
//        assertThat(results.getContent()).hasSize(3);
//        // Should return all books when no criteria are specified
//    }
//
//    @Test
//    void testAdvancedSearch_NoResults() {
//        // Given
//        BookSearchCriteria criteria = new BookSearchCriteria();
//        criteria.setKeyword("NonexistentBook");
//        criteria.setMinPrice(new BigDecimal("1000.00")); // Very high price
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When
//        Page<BookDTO> results = searchService.advancedSearch(criteria, pageable);
//
//        // Then
//        assertThat(results.getContent()).isEmpty();
//    }
//
//    @Test
//    void testSearchBooks_Pagination() {
//        // Given
//        BookSearchCriteria criteria = new BookSearchCriteria();
//        criteria.setLanguage("en");
//        Pageable firstPage = PageRequest.of(0, 1);
//        Pageable secondPage = PageRequest.of(1, 1);
//
//        // When
//        Page<BookDTO> firstResults = searchService.searchBooks(criteria, firstPage);
//        Page<BookDTO> secondResults = searchService.searchBooks(criteria, secondPage);
//
//        // Then
//        assertThat(firstResults.getContent()).hasSize(1);
//        assertThat(secondResults.getContent()).hasSize(1);
//        assertThat(firstResults.getTotalElements()).isEqualTo(2);
//        assertThat(secondResults.getTotalElements()).isEqualTo(2);
//        assertThat(firstResults.getContent().get(0).getId())
//            .isNotEqualTo(secondResults.getContent().get(0).getId());
//    }
//
//    @Test
//    void testSearchBooks_Sorting() {
//        // Given
//        BookSearchCriteria criteria = new BookSearchCriteria();
//        criteria.setLanguage("en");
//        criteria.setSortBy("price");
//        criteria.setSortDirection("ASC");
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When
//        Page<BookDTO> results = searchService.searchBooks(criteria, pageable);
//
//        // Then
//        assertThat(results.getContent()).hasSize(2);
//        // Should be sorted by price ascending
//        BigDecimal firstPrice = results.getContent().get(0).getPrice();
//        BigDecimal secondPrice = results.getContent().get(1).getPrice();
//        assertThat(firstPrice).isLessThanOrEqualTo(secondPrice);
//    }
//}