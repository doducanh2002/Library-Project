package com.library.repository;

import com.library.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    private Category category;
    private Author author;
    private Publisher publisher;

    @BeforeEach
    void setUp() {
        // Create test category
        category = new Category();
        category.setName("Technology");
        category.setDescription("Technology books");
        category = entityManager.persistAndFlush(category);

        // Create test author
        author = new Author();
        author.setName("John Doe");
        author.setBiography("Test author biography");
        author.setBirthDate(LocalDate.of(1970, 1, 1));
        author = entityManager.persistAndFlush(author);

        // Create test publisher
        publisher = new Publisher();
        publisher.setName("Test Publisher");
        publisher.setAddress("123 Test Street");
        publisher.setEstablishedYear(2000);
        publisher = entityManager.persistAndFlush(publisher);
    }

    @Test
    void testCreateBook() {
        // Given
        Book book = new Book();
        book.setTitle("Spring Boot in Action");
        book.setIsbn("978-1617292545");
        book.setPublicationYear(2023);
        book.setDescription("A comprehensive guide to Spring Boot");
        book.setLanguage("en");
        book.setNumberOfPages(384);
        book.setTotalCopiesForLoan(5);
        book.setAvailableCopiesForLoan(5);
        book.setIsLendable(true);
        book.setPrice(new BigDecimal("49.99"));
        book.setStockForSale(10);
        book.setIsSellable(true);
        book.setCategory(category);
        book.setPublisher(publisher);

        // When
        Book savedBook = bookRepository.save(book);

        // Then
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo("Spring Boot in Action");
        assertThat(savedBook.getIsbn()).isEqualTo("978-1617292545");
        assertThat(savedBook.getCategory().getName()).isEqualTo("Technology");
        assertThat(savedBook.getPublisher().getName()).isEqualTo("Test Publisher");
    }

    @Test
    void testFindByIsbn() {
        // Given
        Book book = new Book();
        book.setTitle("Java Programming");
        book.setIsbn("978-0134685991");
        book.setTotalCopiesForLoan(3);
        book.setAvailableCopiesForLoan(3);
        entityManager.persistAndFlush(book);

        // When
        Optional<Book> foundBook = bookRepository.findByIsbn("978-0134685991");

        // Then
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("Java Programming");
    }

    @Test
    void testBookAuthorRelationship() {
        // Given
        Book book = new Book();
        book.setTitle("Design Patterns");
        book.setIsbn("978-0201633610");
        book.setTotalCopiesForLoan(2);
        book.setAvailableCopiesForLoan(2);
        
        book.addAuthor(author, "AUTHOR");
        
        // When
        Book savedBook = bookRepository.save(book);
        entityManager.flush();
        entityManager.clear();
        
        // Then
        Optional<Book> foundBook = bookRepository.findByIdWithAuthors(savedBook.getId());
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getBookAuthors()).hasSize(1);
        assertThat(foundBook.get().getBookAuthors().iterator().next().getAuthor().getName()).isEqualTo("John Doe");
    }

    @Test
    void testFindAvailableForLoan() {
        // Given
        Book availableBook = new Book();
        availableBook.setTitle("Available Book");
        availableBook.setIsbn("978-1111111111");
        availableBook.setIsLendable(true);
        availableBook.setAvailableCopiesForLoan(3);
        availableBook.setTotalCopiesForLoan(3);
        entityManager.persistAndFlush(availableBook);

        Book unavailableBook = new Book();
        unavailableBook.setTitle("Unavailable Book");
        unavailableBook.setIsbn("978-2222222222");
        unavailableBook.setIsLendable(true);
        unavailableBook.setAvailableCopiesForLoan(0);
        unavailableBook.setTotalCopiesForLoan(3);
        entityManager.persistAndFlush(unavailableBook);

        // When
        Page<Book> availableBooks = bookRepository.findAvailableForLoan(PageRequest.of(0, 10));

        // Then
        assertThat(availableBooks.getContent()).hasSize(1);
        assertThat(availableBooks.getContent().get(0).getTitle()).isEqualTo("Available Book");
    }

    @Test
    void testSearchByKeyword() {
        // Given
        Book book1 = new Book();
        book1.setTitle("Spring Framework Guide");
        book1.setIsbn("978-3333333333");
        book1.setDescription("Learn Spring Framework");
        book1.setTotalCopiesForLoan(1);
        book1.setAvailableCopiesForLoan(1);
        entityManager.persistAndFlush(book1);

        Book book2 = new Book();
        book2.setTitle("Python Programming");
        book2.setIsbn("978-4444444444");
        book2.setDescription("Master Python");
        book2.setTotalCopiesForLoan(1);
        book2.setAvailableCopiesForLoan(1);
        entityManager.persistAndFlush(book2);

        // When
        Page<Book> searchResults = bookRepository.searchByKeyword("Spring", PageRequest.of(0, 10));

        // Then
        assertThat(searchResults.getContent()).hasSize(1);
        assertThat(searchResults.getContent().get(0).getTitle()).contains("Spring");
    }
}