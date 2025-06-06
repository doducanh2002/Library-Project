package com.library.specification;

import com.library.dto.BookSearchCriteria;
import com.library.entity.*;
import com.library.repository.BookRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookSpecificationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private EntityManager entityManager;

    private Book book1, book2, book3;
    private Category category1, category2;
    private Publisher publisher1, publisher2;
    private Author author1, author2;

    @BeforeEach
    void setUp() {
        // Create test categories
        category1 = new Category();
        category1.setName("Science Fiction");
        category1.setDescription("Science fiction books");
        entityManager.persist(category1);

        category2 = new Category();
        category2.setName("Fantasy");
        category2.setDescription("Fantasy books");
        entityManager.persist(category2);

        // Create test publishers
        publisher1 = new Publisher();
        publisher1.setName("Tech Books Publisher");
        publisher1.setAddress("123 Tech Street");
        entityManager.persist(publisher1);

        publisher2 = new Publisher();
        publisher2.setName("Fiction House");
        publisher2.setAddress("456 Story Lane");
        entityManager.persist(publisher2);

        // Create test authors
        author1 = new Author();
        author1.setName("John Doe");
        author1.setBiography("Famous sci-fi author");
        entityManager.persist(author1);

        author2 = new Author();
        author2.setName("Jane Smith");
        author2.setBiography("Popular fantasy writer");
        entityManager.persist(author2);

        // Create test books
        book1 = new Book();
        book1.setTitle("Java Programming Guide");
        book1.setIsbn("978-0123456789");
        book1.setDescription("Comprehensive guide to Java programming");
        book1.setLanguage("en");
        book1.setPublicationYear(2020);
        book1.setNumberOfPages(500);
        book1.setPrice(new BigDecimal("29.99"));
        book1.setStockForSale(10);
        book1.setIsSellable(true);
        book1.setIsLendable(true);
        book1.setTotalCopiesForLoan(5);
        book1.setAvailableCopiesForLoan(3);
        book1.setCategory(category1);
        book1.setPublisher(publisher1);
        book1.setCreatedAt(LocalDateTime.now().minusDays(10));
        entityManager.persist(book1);

        book2 = new Book();
        book2.setTitle("Spring Boot in Action");
        book2.setIsbn("978-9876543210");
        book2.setDescription("Learn Spring Boot framework");
        book2.setLanguage("en");
        book2.setPublicationYear(2021);
        book2.setNumberOfPages(400);
        book2.setPrice(new BigDecimal("35.50"));
        book2.setStockForSale(0); // Out of stock
        book2.setIsSellable(true);
        book2.setIsLendable(false); // Not lendable
        book2.setTotalCopiesForLoan(0);
        book2.setAvailableCopiesForLoan(0);
        book2.setCategory(category1);
        book2.setPublisher(publisher1);
        book2.setCreatedAt(LocalDateTime.now().minusDays(5));
        entityManager.persist(book2);

        book3 = new Book();
        book3.setTitle("The Magic Realm");
        book3.setIsbn("978-1111111111");
        book3.setDescription("An epic fantasy adventure");
        book3.setLanguage("vi");
        book3.setPublicationYear(2019);
        book3.setNumberOfPages(600);
        book3.setPrice(new BigDecimal("25.00"));
        book3.setStockForSale(15);
        book3.setIsSellable(true);
        book3.setIsLendable(true);
        book3.setTotalCopiesForLoan(8);
        book3.setAvailableCopiesForLoan(6);
        book3.setCategory(category2);
        book3.setPublisher(publisher2);
        book3.setCreatedAt(LocalDateTime.now().minusDays(45)); // Older book
        entityManager.persist(book3);

        // Create book-author relationships
        BookAuthor bookAuthor1 = new BookAuthor();
        bookAuthor1.setBook(book1);
        bookAuthor1.setAuthor(author1);
        bookAuthor1.setAuthorRole("AUTHOR");
        entityManager.persist(bookAuthor1);

        BookAuthor bookAuthor2 = new BookAuthor();
        bookAuthor2.setBook(book3);
        bookAuthor2.setAuthor(author2);
        bookAuthor2.setAuthorRole("AUTHOR");
        entityManager.persist(bookAuthor2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testKeywordSearch() {
        // Test keyword search in title
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setKeyword("Java");

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).contains("Java");
    }

    @Test
    void testKeywordSearchInDescription() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setKeyword("Spring Boot");

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDescription()).contains("Spring Boot");
    }

    @Test
    void testCategoryFilter() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setCategoryIds(Arrays.asList(category1.getId()));

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(book -> book.getCategory().getId().equals(category1.getId()));
    }

    @Test
    void testPublisherFilter() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setPublisherIds(Arrays.asList(publisher2.getId()));

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPublisher().getId()).isEqualTo(publisher2.getId());
    }

    @Test
    void testAuthorFilter() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setAuthorIds(Arrays.asList(author1.getId()));

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Java Programming Guide");
    }

    @Test
    void testLanguageFilter() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setLanguage("vi");

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLanguage()).isEqualTo("vi");
    }

    @Test
    void testPublicationYearFilter() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setPublicationYear(2020);

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPublicationYear()).isEqualTo(2020);
    }

    @Test
    void testPublicationYearRange() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setPublicationYearFrom(2020);
        criteria.setPublicationYearTo(2021);

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(book -> 
            book.getPublicationYear() >= 2020 && book.getPublicationYear() <= 2021);
    }

    @Test
    void testAvailableForLoanFilter() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setAvailableForLoan(true);

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(2); // book1 and book3
        assertThat(results).allMatch(book -> 
            book.getIsLendable() && book.getAvailableCopiesForLoan() > 0);
    }

    @Test
    void testAvailableForSaleFilter() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setAvailableForSale(true);

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(2); // book1 and book3 (book2 out of stock)
        assertThat(results).allMatch(book -> 
            book.getIsSellable() && book.getStockForSale() > 0);
    }

    @Test
    void testPriceRangeFilter() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setMinPrice(new BigDecimal("30.00"));
        criteria.setMaxPrice(new BigDecimal("40.00"));

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(1); // Only book2
        assertThat(results.get(0).getPrice()).isBetween(
            new BigDecimal("30.00"), new BigDecimal("40.00"));
    }

    @Test
    void testPageRangeFilter() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setMinPages(450);
        criteria.setMaxPages(550);

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(1); // Only book1
        assertThat(results.get(0).getNumberOfPages()).isBetween(450, 550);
    }

    @Test
    void testRecentlyAddedFilter() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setRecentlyAdded(true);

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(2); // book1 and book2 (book3 is older than 30 days)
    }

    @Test
    void testCombinedFilters() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setKeyword("Programming");
        criteria.setCategoryIds(Arrays.asList(category1.getId()));
        criteria.setLanguage("en");
        criteria.setIsSellable(true);

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).contains("Java");
    }

    @Test
    void testWithPagination() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setLanguage("en");

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        Pageable pageable = PageRequest.of(0, 1);
        Page<Book> results = bookRepository.findAll(spec, pageable);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getTotalElements()).isEqualTo(2);
        assertThat(results.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testIndividualSpecifications() {
        // Test hasKeyword specification
        Specification<Book> keywordSpec = BookSpecification.hasKeyword("Java");
        List<Book> keywordResults = bookRepository.findAll(keywordSpec);
        assertThat(keywordResults).hasSize(1);

        // Test belongsToCategory specification
        Specification<Book> categorySpec = BookSpecification.belongsToCategory(category1.getId());
        List<Book> categoryResults = bookRepository.findAll(categorySpec);
        assertThat(categoryResults).hasSize(2);

        // Test availableForLoan specification
        Specification<Book> loanSpec = BookSpecification.availableForLoan();
        List<Book> loanResults = bookRepository.findAll(loanSpec);
        assertThat(loanResults).hasSize(2);

        // Test priceRange specification
        Specification<Book> priceSpec = BookSpecification.priceRange(
            new BigDecimal("25.00"), new BigDecimal("30.00"));
        List<Book> priceResults = bookRepository.findAll(priceSpec);
        assertThat(priceResults).hasSize(2);
    }

    @Test
    void testEmptyCriteria() {
        BookSearchCriteria criteria = new BookSearchCriteria();

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).hasSize(3); // Should return all books
    }

    @Test
    void testNoResultsFound() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setKeyword("NonexistentBook");

        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        List<Book> results = bookRepository.findAll(spec);

        assertThat(results).isEmpty();
    }
}