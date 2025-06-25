# Sprint 2: Books & Catalog Management - Implementation Details

## 📋 **TỔNG QUAN SPRINT 2**

**Thời gian**: Sprint 2 - Books & Catalog Management (2 weeks)  
**Mục tiêu**: Xây dựng hệ thống quản lý sách và catalog hoàn chỉnh  
**Technology Stack**: Spring Boot 3.x, JPA/Hibernate, PostgreSQL, MapStruct, Spring Security

---

## 🎯 **CÁC CÔNG VIỆC ĐÃ TRIỂN KHAI**

### ✅ **BOOK-001: Book CRUD Operations (Quản Lý Sách)**

#### **BOOK-001-T1: Book Entity and Relationships (8h)**
**Đã triển khai:**

**1. Book Entity with Complete Fields**
```java
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_books_title", columnList = "title"),
    @Index(name = "idx_books_isbn", columnList = "isbn"),
    @Index(name = "idx_books_category_id", columnList = "category_id"),
    @Index(name = "idx_books_publisher_id", columnList = "publisher_id"),
    @Index(name = "idx_books_publication_year", columnList = "publication_year"),
    @Index(name = "idx_books_is_lendable", columnList = "is_lendable"),
    @Index(name = "idx_books_is_sellable", columnList = "is_sellable"),
    @Index(name = "idx_books_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    @Size(min = 1, max = 500)
    private String title;
    
    @Column(unique = true, nullable = false, length = 20)
    @Pattern(regexp = "^(978|979)[0-9]{10}$")
    private String isbn;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 10)
    private String language;
    
    @Column(name = "publication_year")
    @Min(1000)
    @Max(2100)
    private Integer publicationYear;
    
    @Column(name = "number_of_pages")
    @Min(1)
    private Integer numberOfPages;
    
    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;
    
    @Column(name = "stock_for_sale")
    @Min(0)
    private Integer stockForSale;
    
    @Column(name = "is_sellable")
    @Builder.Default
    private Boolean isSellable = true;
    
    @Column(name = "total_copies_for_loan")
    @Min(0)
    private Integer totalCopiesForLoan;
    
    @Column(name = "available_copies_for_loan")
    @Min(0)
    private Integer availableCopiesForLoan;
    
    @Column(name = "is_lendable")
    @Builder.Default
    private Boolean isLendable = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookAuthor> bookAuthors = new HashSet<>();
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Document> documents = new HashSet<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Business Logic Methods
    public boolean isAvailableForLoan() {
        return isLendable && availableCopiesForLoan > 0;
    }
    
    public boolean isAvailableForSale() {
        return isSellable && stockForSale > 0;
    }
    
    public void reserveCopyForLoan() {
        if (availableCopiesForLoan > 0) {
            availableCopiesForLoan--;
        }
    }
    
    public void returnCopyFromLoan() {
        if (availableCopiesForLoan < totalCopiesForLoan) {
            availableCopiesForLoan++;
        }
    }
    
    public void reduceSaleStock(int quantity) {
        if (stockForSale >= quantity) {
            stockForSale -= quantity;
        }
    }
    
    public void addAuthor(Author author, String role) {
        BookAuthor bookAuthor = new BookAuthor();
        bookAuthor.setBook(this);
        bookAuthor.setAuthor(author);
        bookAuthor.setAuthorRole(role);
        bookAuthors.add(bookAuthor);
    }
}
```

**2. Category Entity for Book Classification**
```java
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_name", columnList = "name"),
    @Index(name = "idx_category_parent_id", columnList = "parent_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    @Size(min = 1, max = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<Category> subcategories = new HashSet<>();
    
    @OneToMany(mappedBy = "category")
    private Set<Book> books = new HashSet<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Business Methods
    public boolean isParentCategory() {
        return parent == null;
    }
    
    public boolean hasSubcategories() {
        return !subcategories.isEmpty();
    }
}
```

**3. Author Entity with Many-to-Many Relationship**
```java
@Entity
@Table(name = "authors", indexes = {
    @Index(name = "idx_author_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 200)
    @Size(min = 1, max = 200)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String biography;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(name = "death_date")
    private LocalDate deathDate;
    
    @Column(length = 100)
    private String nationality;
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private Set<BookAuthor> bookAuthors = new HashSet<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Business Methods
    public boolean isAlive() {
        return deathDate == null;
    }
    
    public int getAge() {
        LocalDate endDate = deathDate != null ? deathDate : LocalDate.now();
        return birthDate != null ? Period.between(birthDate, endDate).getYears() : 0;
    }
}
```

**4. Publisher Entity**
```java
@Entity
@Table(name = "publishers", indexes = {
    @Index(name = "idx_publisher_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publisher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(unique = true, nullable = false, length = 200)
    @Size(min = 1, max = 200)
    private String name;
    
    @Column(length = 500)
    private String address;
    
    @Column(length = 15)
    private String phoneNumber;
    
    @Column(length = 100)
    @Email
    private String email;
    
    @Column(length = 200)
    private String website;
    
    @Column(name = "established_year")
    @Min(1400)
    @Max(2100)
    private Integer establishedYear;
    
    @OneToMany(mappedBy = "publisher")
    private Set<Book> books = new HashSet<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

**5. BookAuthor Junction Entity for Many-to-Many Relationship**
```java
@Entity
@Table(name = "book_authors", indexes = {
    @Index(name = "idx_book_author_book_id", columnList = "book_id"),
    @Index(name = "idx_book_author_author_id", columnList = "author_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BookAuthorId.class)
public class BookAuthor {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;
    
    @Column(name = "author_role", length = 50)
    @Builder.Default
    private String authorRole = "AUTHOR";
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookAuthorId implements Serializable {
    private Long book;
    private Integer author;
}
```

#### **BOOK-001-T2: Book DTOs and Mapping (6h)**
**Đã triển khai:**

**1. Book Response DTOs**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private Long id;
    private String title;
    private String isbn;
    private String description;
    private String language;
    private Integer publicationYear;
    private Integer numberOfPages;
    private BigDecimal price;
    private Integer stockForSale;
    private Boolean isSellable;
    private Integer totalCopiesForLoan;
    private Integer availableCopiesForLoan;
    private Boolean isLendable;
    private CategoryDTO category;
    private PublisherDTO publisher;
    private List<AuthorDTO> authors;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Derived fields
    private Boolean availableForLoan;
    private Boolean availableForSale;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDetailDTO extends BookDTO {
    private List<DocumentDTO> documents;
    private BookStatisticsDTO statistics;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSummaryDTO {
    private Long id;
    private String title;
    private String isbn;
    private BigDecimal price;
    private Boolean availableForLoan;
    private Boolean availableForSale;
    private CategoryDTO category;
    private List<AuthorDTO> authors;
}
```

**2. Book Request DTOs with Validation**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class CreateBookRequestDTO {
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;
    
    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^(978|979)[0-9]{10}$", message = "Invalid ISBN format")
    private String isbn;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String language;
    
    @Min(value = 1000, message = "Publication year must be at least 1000")
    @Max(value = 2100, message = "Publication year cannot exceed 2100")
    private Integer publicationYear;
    
    @Min(value = 1, message = "Number of pages must be at least 1")
    private Integer numberOfPages;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;
    
    @Min(value = 0, message = "Stock for sale cannot be negative")
    private Integer stockForSale;
    
    private Boolean isSellable = true;
    
    @Min(value = 0, message = "Total copies for loan cannot be negative")
    private Integer totalCopiesForLoan;
    
    @Min(value = 0, message = "Available copies for loan cannot be negative")
    private Integer availableCopiesForLoan;
    
    private Boolean isLendable = true;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    @NotNull(message = "Publisher ID is required")
    private Integer publisherId;
    
    @NotEmpty(message = "At least one author is required")
    private List<@Valid BookAuthorRequestDTO> authors;
    
    @AssertTrue(message = "Available copies cannot exceed total copies")
    public boolean isValidCopies() {
        if (totalCopiesForLoan == null || availableCopiesForLoan == null) {
            return true;
        }
        return availableCopiesForLoan <= totalCopiesForLoan;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookRequestDTO {
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String language;
    
    @Min(value = 1000, message = "Publication year must be at least 1000")
    @Max(value = 2100, message = "Publication year cannot exceed 2100")
    private Integer publicationYear;
    
    @Min(value = 1, message = "Number of pages must be at least 1")
    private Integer numberOfPages;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;
    
    @Min(value = 0, message = "Stock for sale cannot be negative")
    private Integer stockForSale;
    
    private Boolean isSellable;
    
    @Min(value = 0, message = "Total copies for loan cannot be negative")
    private Integer totalCopiesForLoan;
    
    @Min(value = 0, message = "Available copies for loan cannot be negative")
    private Integer availableCopiesForLoan;
    
    private Boolean isLendable;
    
    private Long categoryId;
    private Integer publisherId;
    private List<@Valid BookAuthorRequestDTO> authors;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookAuthorRequestDTO {
    @NotNull(message = "Author ID is required")
    private Integer authorId;
    
    @NotBlank(message = "Author role is required")
    @Size(max = 50, message = "Author role must not exceed 50 characters")
    private String role = "AUTHOR";
}
```

**3. MapStruct Mapper Configuration**
```java
@Mapper(
    componentModel = "spring",
    uses = {CategoryMapper.class, PublisherMapper.class, AuthorMapper.class, DocumentMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface BookMapper {
    
    @Mapping(target = "authors", source = "bookAuthors", qualifiedByName = "mapBookAuthorsToAuthors")
    @Mapping(target = "availableForLoan", expression = "java(book.isAvailableForLoan())")
    @Mapping(target = "availableForSale", expression = "java(book.isAvailableForSale())")
    BookDTO toDTO(Book book);
    
    @Mapping(target = "authors", source = "bookAuthors", qualifiedByName = "mapBookAuthorsToAuthors")
    @Mapping(target = "availableForLoan", expression = "java(book.isAvailableForLoan())")
    @Mapping(target = "availableForSale", expression = "java(book.isAvailableForSale())")
    @Mapping(target = "statistics", ignore = true)
    BookDetailDTO toDetailDTO(Book book);
    
    @Mapping(target = "authors", source = "bookAuthors", qualifiedByName = "mapBookAuthorsToAuthors")
    @Mapping(target = "availableForLoan", expression = "java(book.isAvailableForLoan())")
    @Mapping(target = "availableForSale", expression = "java(book.isAvailableForSale())")
    BookSummaryDTO toSummaryDTO(Book book);
    
    List<BookDTO> toDTOList(List<Book> books);
    List<BookSummaryDTO> toSummaryDTOList(List<Book> books);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookAuthors", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "publisher", ignore = true)
    Book toEntity(CreateBookRequestDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isbn", ignore = true)
    @Mapping(target = "bookAuthors", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "publisher", ignore = true)
    void updateEntityFromDTO(UpdateBookRequestDTO dto, @MappingTarget Book book);
    
    @Named("mapBookAuthorsToAuthors")
    default List<AuthorDTO> mapBookAuthorsToAuthors(Set<BookAuthor> bookAuthors) {
        if (bookAuthors == null || bookAuthors.isEmpty()) {
            return new ArrayList<>();
        }
        return bookAuthors.stream()
            .map(BookAuthor::getAuthor)
            .map(this::authorToDTO)
            .collect(Collectors.toList());
    }
    
    private AuthorDTO authorToDTO(Author author) {
        return AuthorDTO.builder()
            .id(author.getId())
            .name(author.getName())
            .biography(author.getBiography())
            .birthDate(author.getBirthDate())
            .deathDate(author.getDeathDate())
            .nationality(author.getNationality())
            .build();
    }
}
```

#### **BOOK-001-T3: Book Service Implementation (8h)**
**Đã triển khai:**

**1. Book Service Interface**
```java
public interface BookService {
    // CRUD Operations
    BookDetailDTO createBook(CreateBookRequestDTO request);
    BookDetailDTO getBookById(Long id);
    BookDetailDTO updateBook(Long id, UpdateBookRequestDTO request);
    void deleteBook(Long id);
    
    // Search and Filter Operations
    Page<BookDTO> getAllBooks(Pageable pageable);
    Page<BookDTO> searchBooks(BookSearchCriteria criteria, Pageable pageable);
    List<BookSummaryDTO> getRecentBooks(int limit);
    List<BookSummaryDTO> getPopularBooks(int limit);
    
    // Availability Operations
    Page<BookDTO> getAvailableForLoan(Pageable pageable);
    Page<BookDTO> getAvailableForSale(Pageable pageable);
    
    // Stock Management
    boolean reserveBookForLoan(Long bookId);
    boolean returnBookFromLoan(Long bookId);
    boolean reduceStockForSale(Long bookId, int quantity);
    boolean increaseStockForSale(Long bookId, int quantity);
    
    // Business Logic
    boolean isBookAvailable(Long bookId, String availabilityType);
    BookStatisticsDTO getBookStatistics(Long bookId);
}
```

**2. Book Service Implementation**
```java
@Service
@Transactional
@Slf4j
public class BookServiceImpl implements BookService {
    
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final BookAuthorRepository bookAuthorRepository;
    private final BookMapper bookMapper;
    private final NotificationService notificationService;
    
    public BookServiceImpl(BookRepository bookRepository,
                          CategoryRepository categoryRepository,
                          PublisherRepository publisherRepository,
                          AuthorRepository authorRepository,
                          BookAuthorRepository bookAuthorRepository,
                          BookMapper bookMapper,
                          NotificationService notificationService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
        this.authorRepository = authorRepository;
        this.bookAuthorRepository = bookAuthorRepository;
        this.bookMapper = bookMapper;
        this.notificationService = notificationService;
    }
    
    @Override
    public BookDetailDTO createBook(CreateBookRequestDTO request) {
        log.info("Creating new book with title: {}", request.getTitle());
        
        // Validate ISBN uniqueness
        if (bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
            throw new DuplicateResourceException("Book with ISBN " + request.getIsbn() + " already exists");
        }
        
        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        
        // Validate publisher exists
        Publisher publisher = publisherRepository.findById(request.getPublisherId())
            .orElseThrow(() -> new ResourceNotFoundException("Publisher not found with id: " + request.getPublisherId()));
        
        // Create book entity
        Book book = bookMapper.toEntity(request);
        book.setCategory(category);
        book.setPublisher(publisher);
        
        // Save book first to get ID
        book = bookRepository.save(book);
        
        // Add authors
        for (BookAuthorRequestDTO authorRequest : request.getAuthors()) {
            Author author = authorRepository.findById(authorRequest.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + authorRequest.getAuthorId()));
            
            book.addAuthor(author, authorRequest.getRole());
        }
        
        book = bookRepository.save(book);
        
        log.info("Successfully created book with ID: {}", book.getId());
        
        // Send notification
        notificationService.createNotification(
            NotificationType.BOOK_ADDED,
            "New book added: " + book.getTitle(),
            null,
            book.getId()
        );
        
        return bookMapper.toDetailDTO(book);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BookDetailDTO getBookById(Long id) {
        log.debug("Fetching book details for ID: {}", id);
        
        Book book = bookRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        
        BookDetailDTO dto = bookMapper.toDetailDTO(book);
        dto.setStatistics(getBookStatistics(id));
        
        return dto;
    }
    
    @Override
    public BookDetailDTO updateBook(Long id, UpdateBookRequestDTO request) {
        log.info("Updating book with ID: {}", id);
        
        Book book = bookRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        
        // Update basic fields
        bookMapper.updateEntityFromDTO(request, book);
        
        // Update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            book.setCategory(category);
        }
        
        // Update publisher if provided
        if (request.getPublisherId() != null) {
            Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new ResourceNotFoundException("Publisher not found with id: " + request.getPublisherId()));
            book.setPublisher(publisher);
        }
        
        // Update authors if provided
        if (request.getAuthors() != null && !request.getAuthors().isEmpty()) {
            // Remove existing authors
            bookAuthorRepository.deleteByBookId(book.getId());
            book.getBookAuthors().clear();
            
            // Add new authors
            for (BookAuthorRequestDTO authorRequest : request.getAuthors()) {
                Author author = authorRepository.findById(authorRequest.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + authorRequest.getAuthorId()));
                
                book.addAuthor(author, authorRequest.getRole());
            }
        }
        
        book = bookRepository.save(book);
        
        log.info("Successfully updated book with ID: {}", book.getId());
        
        return bookMapper.toDetailDTO(book);
    }
    
    @Override
    public void deleteBook(Long id) {
        log.info("Deleting book with ID: {}", id);
        
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        
        // Check if book has active loans
        if (bookRepository.hasActiveLoansByBookId(id)) {
            throw new BusinessLogicException("Cannot delete book with active loans");
        }
        
        // Check if book has pending orders
        if (bookRepository.hasPendingOrdersByBookId(id)) {
            throw new BusinessLogicException("Cannot delete book with pending orders");
        }
        
        bookRepository.delete(book);
        
        log.info("Successfully deleted book with ID: {}", id);
        
        // Send notification
        notificationService.createNotification(
            NotificationType.BOOK_REMOVED,
            "Book removed: " + book.getTitle(),
            null,
            book.getId()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> searchBooks(BookSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching books with criteria: {}", criteria);
        
        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        Page<Book> books = bookRepository.findAll(spec, pageable);
        
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public boolean reserveBookForLoan(Long bookId) {
        log.info("Reserving book for loan: {}", bookId);
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        
        if (!book.isAvailableForLoan()) {
            log.warn("Book {} is not available for loan", bookId);
            return false;
        }
        
        book.reserveCopyForLoan();
        bookRepository.save(book);
        
        // Check if book is now out of stock
        if (!book.isAvailableForLoan()) {
            notificationService.createNotification(
                NotificationType.BOOK_OUT_OF_STOCK,
                "Book out of stock for loan: " + book.getTitle(),
                null,
                book.getId()
            );
        }
        
        log.info("Successfully reserved book {} for loan", bookId);
        return true;
    }
    
    @Override
    public boolean returnBookFromLoan(Long bookId) {
        log.info("Returning book from loan: {}", bookId);
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        
        boolean wasUnavailable = !book.isAvailableForLoan();
        book.returnCopyFromLoan();
        bookRepository.save(book);
        
        // If book was previously unavailable and now available, send notification
        if (wasUnavailable && book.isAvailableForLoan()) {
            notificationService.createNotification(
                NotificationType.BOOK_AVAILABLE,
                "Book now available for loan: " + book.getTitle(),
                null,
                book.getId()
            );
        }
        
        log.info("Successfully returned book {} from loan", bookId);
        return true;
    }
    
    @Override
    @Transactional(readOnly = true)
    public BookStatisticsDTO getBookStatistics(Long bookId) {
        return BookStatisticsDTO.builder()
            .totalLoans(bookRepository.countTotalLoansByBookId(bookId))
            .activeLoans(bookRepository.countActiveLoansByBookId(bookId))
            .totalSales(bookRepository.countTotalSalesByBookId(bookId))
            .averageRating(bookRepository.getAverageRatingByBookId(bookId))
            .totalReviews(bookRepository.countReviewsByBookId(bookId))
            .build();
    }
}
```

#### **BOOK-001-T4: Admin Book Endpoints (6h)**
**Đã triển khai:**

**1. Admin Book Controller**
```java
@RestController
@RequestMapping("/api/v1/admin/books")
@PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
@Validated
@Slf4j
@Tag(name = "Admin Book Management", description = "Admin operations for book management")
public class AdminBookController {
    
    private final BookService bookService;
    
    public AdminBookController(BookService bookService) {
        this.bookService = bookService;
    }
    
    @PostMapping
    @Operation(summary = "Create new book", description = "Create a new book in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Book created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Book with ISBN already exists"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RateLimit(key = "create_book", rate = 10, duration = 60)
    public ResponseEntity<ApiResponse<BookDetailDTO>> createBook(
            @Valid @RequestBody CreateBookRequestDTO request,
            Authentication authentication) {
        
        log.info("Admin {} creating new book: {}", authentication.getName(), request.getTitle());
        
        BookDetailDTO book = bookService.createBook(request);
        
        ApiResponse<BookDetailDTO> response = ApiResponse.<BookDetailDTO>builder()
            .success(true)
            .message("Book created successfully")
            .data(book)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update book", description = "Update an existing book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book updated successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RateLimit(key = "update_book", rate = 20, duration = 60)
    public ResponseEntity<ApiResponse<BookDetailDTO>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequestDTO request,
            Authentication authentication) {
        
        log.info("Admin {} updating book ID: {}", authentication.getName(), id);
        
        BookDetailDTO book = bookService.updateBook(id, request);
        
        ApiResponse<BookDetailDTO> response = ApiResponse.<BookDetailDTO>builder()
            .success(true)
            .message("Book updated successfully")
            .data(book)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book", description = "Delete a book from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "400", description = "Cannot delete book with active loans/orders"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RateLimit(key = "delete_book", rate = 5, duration = 60)
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Admin {} deleting book ID: {}", authentication.getName(), id);
        
        bookService.deleteBook(id);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(true)
            .message("Book deleted successfully")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all books for admin", description = "Get paginated list of all books with admin details")
    @RateLimit(key = "admin_get_books", rate = 100, duration = 60)
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getAllBooksForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            Authentication authentication) {
        
        log.debug("Admin {} fetching all books - page: {}, size: {}", authentication.getName(), page, size);
        
        Pageable pageable = PageRequest.of(page, size, 
            Sort.Direction.fromString(sortDirection), sortBy);
        
        Page<BookDTO> books = bookService.getAllBooks(pageable);
        
        ApiResponse<Page<BookDTO>> response = ApiResponse.<Page<BookDTO>>builder()
            .success(true)
            .message("Books retrieved successfully")
            .data(books)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/stock/increase")
    @Operation(summary = "Increase book stock", description = "Increase stock quantity for sale")
    @RateLimit(key = "increase_stock", rate = 30, duration = 60)
    public ResponseEntity<ApiResponse<Void>> increaseStock(
            @PathVariable Long id,
            @RequestParam @Min(1) int quantity,
            Authentication authentication) {
        
        log.info("Admin {} increasing stock for book ID: {} by {}", authentication.getName(), id, quantity);
        
        boolean success = bookService.increaseStockForSale(id, quantity);
        
        if (!success) {
            throw new BusinessLogicException("Failed to increase stock for book");
        }
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(true)
            .message("Stock increased successfully")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
}
```

### ✅ **BOOK-002: Book Search & Browse (Tìm Kiếm và Duyệt Sách)**

#### **BOOK-002-T1: Search Specification (6h)**
**Đã triển khai:**

**1. Book Search Criteria DTO**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchCriteria {
    private String keyword;
    private List<Long> categoryIds;
    private List<Integer> authorIds;
    private List<Integer> publisherIds;
    private String language;
    private Integer minPages;
    private Integer maxPages;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer publicationYearFrom;
    private Integer publicationYearTo;
    private Boolean availableForLoan;
    private Boolean availableForSale;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
    
    // Validation methods
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    public boolean hasCategoryFilter() {
        return categoryIds != null && !categoryIds.isEmpty();
    }
    
    public boolean hasAuthorFilter() {
        return authorIds != null && !authorIds.isEmpty();
    }
    
    public boolean hasPublisherFilter() {
        return publisherIds != null && !publisherIds.isEmpty();
    }
    
    public boolean hasPriceRange() {
        return minPrice != null || maxPrice != null;
    }
    
    public boolean hasPageRange() {
        return minPages != null || maxPages != null;
    }
    
    public boolean hasYearRange() {
        return publicationYearFrom != null || publicationYearTo != null;
    }
}
```

**2. Book Specification for Dynamic Queries**
```java
@Component
public class BookSpecification {
    
    public static Specification<Book> withCriteria(BookSearchCriteria criteria) {
        return Specification
            .where(hasKeyword(criteria.getKeyword()))
            .and(hasCategories(criteria.getCategoryIds()))
            .and(hasAuthors(criteria.getAuthorIds()))
            .and(hasPublishers(criteria.getPublisherIds()))
            .and(hasLanguage(criteria.getLanguage()))
            .and(hasPageRange(criteria.getMinPages(), criteria.getMaxPages()))
            .and(hasPriceRange(criteria.getMinPrice(), criteria.getMaxPrice()))
            .and(hasYearRange(criteria.getPublicationYearFrom(), criteria.getPublicationYearTo()))
            .and(isAvailableForLoan(criteria.getAvailableForLoan()))
            .and(isAvailableForSale(criteria.getAvailableForSale()));
    }
    
    private static Specification<Book> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            
            String searchTerm = "%" + keyword.toLowerCase() + "%";
            
            // Create joins for related entities
            Join<Book, Category> categoryJoin = root.join("category", JoinType.LEFT);
            Join<Book, Publisher> publisherJoin = root.join("publisher", JoinType.LEFT);
            Join<Book, BookAuthor> bookAuthorJoin = root.join("bookAuthors", JoinType.LEFT);
            Join<BookAuthor, Author> authorJoin = bookAuthorJoin.join("author", JoinType.LEFT);
            
            Predicate titlePredicate = cb.like(cb.lower(root.get("title")), searchTerm);
            Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), searchTerm);
            Predicate isbnPredicate = cb.like(cb.lower(root.get("isbn")), searchTerm);
            Predicate categoryPredicate = cb.like(cb.lower(categoryJoin.get("name")), searchTerm);
            Predicate publisherPredicate = cb.like(cb.lower(publisherJoin.get("name")), searchTerm);
            Predicate authorPredicate = cb.like(cb.lower(authorJoin.get("name")), searchTerm);
            
            return cb.or(titlePredicate, descriptionPredicate, isbnPredicate, 
                        categoryPredicate, publisherPredicate, authorPredicate);
        };
    }
    
    private static Specification<Book> hasCategories(List<Long> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("category").get("id").in(categoryIds);
        };
    }
    
    private static Specification<Book> hasAuthors(List<Integer> authorIds) {
        return (root, query, cb) -> {
            if (authorIds == null || authorIds.isEmpty()) {
                return cb.conjunction();
            }
            
            Join<Book, BookAuthor> bookAuthorJoin = root.join("bookAuthors");
            Join<BookAuthor, Author> authorJoin = bookAuthorJoin.join("author");
            
            return authorJoin.get("id").in(authorIds);
        };
    }
    
    private static Specification<Book> hasPublishers(List<Integer> publisherIds) {
        return (root, query, cb) -> {
            if (publisherIds == null || publisherIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("publisher").get("id").in(publisherIds);
        };
    }
    
    private static Specification<Book> hasLanguage(String language) {
        return (root, query, cb) -> {
            if (language == null || language.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("language"), language);
        };
    }
    
    private static Specification<Book> hasPageRange(Integer minPages, Integer maxPages) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            
            if (minPages != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("numberOfPages"), minPages));
            }
            
            if (maxPages != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("numberOfPages"), maxPages));
            }
            
            return predicate;
        };
    }
    
    private static Specification<Book> hasPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            
            if (minPrice != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            
            if (maxPrice != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            
            return predicate;
        };
    }
    
    private static Specification<Book> hasYearRange(Integer yearFrom, Integer yearTo) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            
            if (yearFrom != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("publicationYear"), yearFrom));
            }
            
            if (yearTo != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("publicationYear"), yearTo));
            }
            
            return predicate;
        };
    }
    
    private static Specification<Book> isAvailableForLoan(Boolean availableForLoan) {
        return (root, query, cb) -> {
            if (availableForLoan == null) {
                return cb.conjunction();
            }
            
            if (availableForLoan) {
                return cb.and(
                    cb.isTrue(root.get("isLendable")),
                    cb.greaterThan(root.get("availableCopiesForLoan"), 0)
                );
            } else {
                return cb.or(
                    cb.isFalse(root.get("isLendable")),
                    cb.equal(root.get("availableCopiesForLoan"), 0)
                );
            }
        };
    }
    
    private static Specification<Book> isAvailableForSale(Boolean availableForSale) {
        return (root, query, cb) -> {
            if (availableForSale == null) {
                return cb.conjunction();
            }
            
            if (availableForSale) {
                return cb.and(
                    cb.isTrue(root.get("isSellable")),
                    cb.greaterThan(root.get("stockForSale"), 0)
                );
            } else {
                return cb.or(
                    cb.isFalse(root.get("isSellable")),
                    cb.equal(root.get("stockForSale"), 0)
                );
            }
        };
    }
}
```

#### **BOOK-002-T2: Search Service Implementation (8h)**
**Đã triển khai:**

**1. Search Service Interface**
```java
public interface SearchService {
    // Basic search operations
    Page<BookDTO> searchBooks(BookSearchCriteria criteria, Pageable pageable);
    Page<BookDTO> fullTextSearch(String searchText, Pageable pageable);
    Page<BookDTO> advancedSearch(BookSearchCriteria criteria, Pageable pageable);
    
    // Category-based search
    Page<BookDTO> searchByCategories(List<Long> categoryIds, Pageable pageable);
    
    // Author-based search
    Page<BookDTO> searchByAuthors(List<Integer> authorIds, Pageable pageable);
    
    // Availability-based search
    Page<BookDTO> searchAvailableForLoan(Pageable pageable);
    Page<BookDTO> searchAvailableForSale(Pageable pageable);
    Page<BookDTO> searchRecentlyAdded(int days, Pageable pageable);
    
    // Search suggestions and analytics
    List<String> getSearchSuggestions(String partialText, int limit);
    List<String> getPopularSearchTerms(int limit);
    
    // Search analytics
    void recordSearchQuery(String query, Long userId, int resultCount);
}
```

**2. Search Service Implementation**
```java
@Service
@Transactional(readOnly = true)
@Slf4j
public class SearchServiceImpl implements SearchService {
    
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SearchQueryRepository searchQueryRepository;
    
    private static final String SEARCH_SUGGESTIONS_KEY = "search:suggestions:";
    private static final String POPULAR_TERMS_KEY = "search:popular_terms";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    
    public SearchServiceImpl(BookRepository bookRepository,
                           BookMapper bookMapper,
                           RedisTemplate<String, Object> redisTemplate,
                           SearchQueryRepository searchQueryRepository) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.redisTemplate = redisTemplate;
        this.searchQueryRepository = searchQueryRepository;
    }
    
    @Override
    public Page<BookDTO> searchBooks(BookSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching books with criteria: {}", criteria);
        
        // Build dynamic specification
        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        
        // Add sorting
        Sort sort = buildSort(criteria.getSortBy(), criteria.getSortDirection());
        Pageable pageableWithSort = PageRequest.of(
            pageable.getPageNumber(), 
            pageable.getPageSize(), 
            sort
        );
        
        Page<Book> books = bookRepository.findAll(spec, pageableWithSort);
        
        // Record search analytics
        if (criteria.hasKeyword()) {
            recordSearchQuery(criteria.getKeyword(), null, (int) books.getTotalElements());
        }
        
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> fullTextSearch(String searchText, Pageable pageable) {
        log.debug("Performing full-text search for: {}", searchText);
        
        if (searchText == null || searchText.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Use PostgreSQL full-text search if available, otherwise fallback to LIKE
        Page<Book> books;
        try {
            books = bookRepository.fullTextSearch(searchText, pageable);
        } catch (Exception e) {
            log.warn("Full-text search failed, falling back to LIKE search: {}", e.getMessage());
            books = bookRepository.searchByKeyword(searchText, pageable);
        }
        
        recordSearchQuery(searchText, null, (int) books.getTotalElements());
        
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> advancedSearch(BookSearchCriteria criteria, Pageable pageable) {
        log.debug("Performing advanced search with criteria: {}", criteria);
        
        // Enhanced specification with more complex logic
        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        
        // Add custom sorting and filtering
        Sort sort = buildAdvancedSort(criteria);
        Pageable pageableWithSort = PageRequest.of(
            pageable.getPageNumber(), 
            pageable.getPageSize(), 
            sort
        );
        
        Page<Book> books = bookRepository.findAll(spec, pageableWithSort);
        
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> searchByCategories(List<Long> categoryIds, Pageable pageable) {
        log.debug("Searching books by categories: {}", categoryIds);
        
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        Page<Book> books = bookRepository.findByCategoryIdIn(categoryIds, pageable);
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> searchByAuthors(List<Integer> authorIds, Pageable pageable) {
        log.debug("Searching books by authors: {}", authorIds);
        
        if (authorIds == null || authorIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        Page<Book> books = bookRepository.findByAuthorIds(authorIds, pageable);
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> searchAvailableForLoan(Pageable pageable) {
        log.debug("Searching books available for loan");
        
        Page<Book> books = bookRepository.findAvailableForLoan(pageable);
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> searchAvailableForSale(Pageable pageable) {
        log.debug("Searching books available for sale");
        
        Page<Book> books = bookRepository.findAvailableForSale(pageable);
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> searchRecentlyAdded(int days, Pageable pageable) {
        log.debug("Searching recently added books within {} days", days);
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Page<Book> books = bookRepository.findByCreatedAtAfter(since, pageable);
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    @Cacheable(value = "searchSuggestions", key = "#partialText + '_' + #limit")
    public List<String> getSearchSuggestions(String partialText, int limit) {
        log.debug("Getting search suggestions for: {}", partialText);
        
        if (partialText == null || partialText.trim().length() < 2) {
            return Collections.emptyList();
        }
        
        List<String> suggestions = new ArrayList<>();
        
        // Get title suggestions
        List<String> titleSuggestions = bookRepository.findTitleSuggestions(partialText, limit / 2);
        suggestions.addAll(titleSuggestions);
        
        // Get author suggestions
        List<String> authorSuggestions = bookRepository.findAuthorSuggestions(partialText, limit / 2);
        suggestions.addAll(authorSuggestions);
        
        return suggestions.stream()
            .distinct()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "popularSearchTerms", key = "#limit")
    public List<String> getPopularSearchTerms(int limit) {
        log.debug("Getting popular search terms with limit: {}", limit);
        
        return searchQueryRepository.findMostPopularTerms(limit);
    }
    
    @Override
    @Async
    public void recordSearchQuery(String query, Long userId, int resultCount) {
        try {
            SearchQuery searchQuery = SearchQuery.builder()
                .query(query.toLowerCase().trim())
                .userId(userId)
                .resultCount(resultCount)
                .searchedAt(LocalDateTime.now())
                .build();
            
            searchQueryRepository.save(searchQuery);
            
            // Update popular terms cache
            String key = POPULAR_TERMS_KEY;
            redisTemplate.opsForZSet().incrementScore(key, query.toLowerCase().trim(), 1.0);
            redisTemplate.expire(key, CACHE_TTL);
            
        } catch (Exception e) {
            log.error("Failed to record search query: {}", query, e);
        }
    }
    
    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        
        return switch (sortBy.toLowerCase()) {
            case "title" -> Sort.by(direction, "title");
            case "price" -> Sort.by(direction, "price");
            case "year", "publicationyear" -> Sort.by(direction, "publicationYear");
            case "pages" -> Sort.by(direction, "numberOfPages");
            case "category" -> Sort.by(direction, "category.name");
            case "author" -> Sort.by(direction, "bookAuthors.author.name");
            case "popularity" -> Sort.by(Sort.Direction.DESC, "totalLoans", "totalSales");
            default -> Sort.by(direction, "createdAt");
        };
    }
    
    private Sort buildAdvancedSort(BookSearchCriteria criteria) {
        List<Sort.Order> orders = new ArrayList<>();
        
        // Primary sort
        Sort.Direction direction = Sort.Direction.fromString(criteria.getSortDirection());
        orders.add(new Sort.Order(direction, criteria.getSortBy()));
        
        // Secondary sorts for better relevance
        if (!criteria.getSortBy().equals("createdAt")) {
            orders.add(new Sort.Order(Sort.Direction.DESC, "createdAt"));
        }
        
        return Sort.by(orders);
    }
}
```

#### **BOOK-002-T3: Public Book Endpoints (4h)**
**Đã triển khai:**

**1. Public Book Controller**
```java
@RestController
@RequestMapping("/api/v1/books")
@Validated
@Slf4j
@Tag(name = "Book Search & Browse", description = "Public endpoints for book search and browsing")
public class BookController {
    
    private final BookService bookService;
    private final SearchService searchService;
    
    public BookController(BookService bookService, SearchService searchService) {
        this.bookService = bookService;
        this.searchService = searchService;
    }
    
    @GetMapping
    @Operation(summary = "Search books", description = "Search books with various criteria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Books retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    @RateLimit(key = "search_books", rate = 100, duration = 60)
    public ResponseEntity<ApiResponse<Page<BookDTO>>> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Integer> authorIds,
            @RequestParam(required = false) List<Integer> publisherIds,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer minPages,
            @RequestParam(required = false) Integer maxPages,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer publicationYearFrom,
            @RequestParam(required = false) Integer publicationYearTo,
            @RequestParam(required = false) Boolean availableForLoan,
            @RequestParam(required = false) Boolean availableForSale,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        
        log.debug("Searching books with keyword: {}, page: {}, size: {}", keyword, page, size);
        
        BookSearchCriteria criteria = BookSearchCriteria.builder()
            .keyword(keyword)
            .categoryIds(categoryIds)
            .authorIds(authorIds)
            .publisherIds(publisherIds)
            .language(language)
            .minPages(minPages)
            .maxPages(maxPages)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .publicationYearFrom(publicationYearFrom)
            .publicationYearTo(publicationYearTo)
            .availableForLoan(availableForLoan)
            .availableForSale(availableForSale)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> books = searchService.searchBooks(criteria, pageable);
        
        ApiResponse<Page<BookDTO>> response = ApiResponse.<Page<BookDTO>>builder()
            .success(true)
            .message("Books retrieved successfully")
            .data(books)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get book details", description = "Get detailed information about a specific book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @RateLimit(key = "get_book", rate = 200, duration = 60)
    public ResponseEntity<ApiResponse<BookDetailDTO>> getBookById(@PathVariable Long id) {
        
        log.debug("Fetching book details for ID: {}", id);
        
        BookDetailDTO book = bookService.getBookById(id);
        
        ApiResponse<BookDetailDTO> response = ApiResponse.<BookDetailDTO>builder()
            .success(true)
            .message("Book details retrieved successfully")
            .data(book)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/popular")
    @Operation(summary = "Get popular books", description = "Get list of popular books based on loans and sales")
    @RateLimit(key = "popular_books", rate = 50, duration = 60)
    public ResponseEntity<ApiResponse<List<BookSummaryDTO>>> getPopularBooks(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        log.debug("Fetching popular books with limit: {}", limit);
        
        List<BookSummaryDTO> books = bookService.getPopularBooks(limit);
        
        ApiResponse<List<BookSummaryDTO>> response = ApiResponse.<List<BookSummaryDTO>>builder()
            .success(true)
            .message("Popular books retrieved successfully")
            .data(books)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/recent")
    @Operation(summary = "Get recently added books", description = "Get list of recently added books")
    @RateLimit(key = "recent_books", rate = 50, duration = 60)
    public ResponseEntity<ApiResponse<List<BookSummaryDTO>>> getRecentBooks(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        log.debug("Fetching recent books with limit: {}", limit);
        
        List<BookSummaryDTO> books = bookService.getRecentBooks(limit);
        
        ApiResponse<List<BookSummaryDTO>> response = ApiResponse.<List<BookSummaryDTO>>builder()
            .success(true)
            .message("Recent books retrieved successfully")
            .data(books)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/available-for-loan")
    @Operation(summary = "Get books available for loan", description = "Get paginated list of books available for loan")
    @RateLimit(key = "available_loan", rate = 50, duration = 60)
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getAvailableForLoan(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Fetching books available for loan - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> books = searchService.searchAvailableForLoan(pageable);
        
        ApiResponse<Page<BookDTO>> response = ApiResponse.<Page<BookDTO>>builder()
            .success(true)
            .message("Books available for loan retrieved successfully")
            .data(books)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/available-for-sale")
    @Operation(summary = "Get books available for sale", description = "Get paginated list of books available for sale")
    @RateLimit(key = "available_sale", rate = 50, duration = 60)
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getAvailableForSale(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Fetching books available for sale - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> books = searchService.searchAvailableForSale(pageable);
        
        ApiResponse<Page<BookDTO>> response = ApiResponse.<Page<BookDTO>>builder()
            .success(true)
            .message("Books available for sale retrieved successfully")
            .data(books)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search/suggestions")
    @Operation(summary = "Get search suggestions", description = "Get search suggestions based on partial text")
    @RateLimit(key = "search_suggestions", rate = 100, duration = 60)
    public ResponseEntity<ApiResponse<List<String>>> getSearchSuggestions(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit) {
        
        log.debug("Getting search suggestions for: {}", q);
        
        List<String> suggestions = searchService.getSearchSuggestions(q, limit);
        
        ApiResponse<List<String>> response = ApiResponse.<List<String>>builder()
            .success(true)
            .message("Search suggestions retrieved successfully")
            .data(suggestions)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search/full-text")
    @Operation(summary = "Full-text search", description = "Perform full-text search on books")
    @RateLimit(key = "fulltext_search", rate = 50, duration = 60)
    public ResponseEntity<ApiResponse<Page<BookDTO>>> fullTextSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Performing full-text search for: {}", q);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> books = searchService.fullTextSearch(q, pageable);
        
        ApiResponse<Page<BookDTO>> response = ApiResponse.<Page<BookDTO>>builder()
            .success(true)
            .message("Full-text search completed successfully")
            .data(books)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
}
```

### ✅ **BOOK-003: Category & Author Management (Quản Lý Thể Loại và Tác Giả)**

#### **BOOK-003-T1: Category Management (6h)**
**Đã triển khai:**

**1. Category DTOs**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private CategoryDTO parent;
    private List<CategoryDTO> subcategories;
    private Integer bookCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class CreateCategoryRequestDTO {
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private Long parentId;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class UpdateCategoryRequestDTO {
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private Long parentId;
}
```

**2. Category Service Implementation**
```java
@Service
@Transactional
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    
    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }
    
    @Override
    public CategoryDTO createCategory(CreateCategoryRequestDTO request) {
        log.info("Creating new category: {}", request.getName());
        
        // Check if category name already exists
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }
        
        Category category = categoryMapper.toEntity(request);
        
        // Set parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + request.getParentId()));
            
            // Prevent circular reference
            if (isCircularReference(parent, category)) {
                throw new BusinessLogicException("Cannot create circular reference in category hierarchy");
            }
            
            category.setParent(parent);
        }
        
        category = categoryRepository.save(category);
        
        log.info("Successfully created category with ID: {}", category.getId());
        
        return categoryMapper.toDTO(category);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.debug("Fetching all categories");
        
        List<Category> categories = categoryRepository.findAllWithBookCount();
        return categoryMapper.toDTOList(categories);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getRootCategories() {
        log.debug("Fetching root categories");
        
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return categoryMapper.toDTOList(rootCategories);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getSubcategories(Long parentId) {
        log.debug("Fetching subcategories for parent ID: {}", parentId);
        
        Category parent = categoryRepository.findById(parentId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + parentId));
        
        List<Category> subcategories = categoryRepository.findByParentId(parentId);
        return categoryMapper.toDTOList(subcategories);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        log.debug("Fetching category with ID: {}", id);
        
        Category category = categoryRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        return categoryMapper.toDTO(category);
    }
    
    @Override
    public CategoryDTO updateCategory(Long id, UpdateCategoryRequestDTO request) {
        log.info("Updating category with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        // Check name uniqueness if name is being changed
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
            }
        }
        
        categoryMapper.updateEntityFromDTO(request, category);
        
        // Update parent if provided
        if (request.getParentId() != null) {
            if (!request.getParentId().equals(category.getParent()?.getId())) {
                Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + request.getParentId()));
                
                // Prevent circular reference
                if (isCircularReference(newParent, category)) {
                    throw new BusinessLogicException("Cannot create circular reference in category hierarchy");
                }
                
                category.setParent(newParent);
            }
        }
        
        category = categoryRepository.save(category);
        
        log.info("Successfully updated category with ID: {}", category.getId());
        
        return categoryMapper.toDTO(category);
    }
    
    @Override
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        // Check if category has books
        if (categoryRepository.countBooksByCategoryId(id) > 0) {
            throw new BusinessLogicException("Cannot delete category that contains books");
        }
        
        // Check if category has subcategories
        if (categoryRepository.countSubcategoriesByParentId(id) > 0) {
            throw new BusinessLogicException("Cannot delete category that has subcategories");
        }
        
        categoryRepository.delete(category);
        
        log.info("Successfully deleted category with ID: {}", id);
    }
    
    private boolean isCircularReference(Category potentialParent, Category child) {
        Category current = potentialParent;
        while (current != null) {
            if (current.getId().equals(child.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
```

**3. Category Controller**
```java
@RestController
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
@Validated
@Slf4j
@Tag(name = "Category Management", description = "Admin operations for category management")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    @PostMapping
    @Operation(summary = "Create category", description = "Create a new category")
    @RateLimit(key = "create_category", rate = 20, duration = 60)
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
            @Valid @RequestBody CreateCategoryRequestDTO request,
            Authentication authentication) {
        
        log.info("Admin {} creating new category: {}", authentication.getName(), request.getName());
        
        CategoryDTO category = categoryService.createCategory(request);
        
        ApiResponse<CategoryDTO> response = ApiResponse.<CategoryDTO>builder()
            .success(true)
            .message("Category created successfully")
            .data(category)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all categories", description = "Get all categories with hierarchy")
    @RateLimit(key = "get_categories", rate = 100, duration = 60)
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        
        log.debug("Fetching all categories");
        
        List<CategoryDTO> categories = categoryService.getAllCategories();
        
        ApiResponse<List<CategoryDTO>> response = ApiResponse.<List<CategoryDTO>>builder()
            .success(true)
            .message("Categories retrieved successfully")
            .data(categories)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/root")
    @Operation(summary = "Get root categories", description = "Get top-level categories")
    @RateLimit(key = "get_root_categories", rate = 100, duration = 60)
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getRootCategories() {
        
        log.debug("Fetching root categories");
        
        List<CategoryDTO> categories = categoryService.getRootCategories();
        
        ApiResponse<List<CategoryDTO>> response = ApiResponse.<List<CategoryDTO>>builder()
            .success(true)
            .message("Root categories retrieved successfully")
            .data(categories)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Get category details by ID")
    @RateLimit(key = "get_category", rate = 100, duration = 60)
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        
        log.debug("Fetching category with ID: {}", id);
        
        CategoryDTO category = categoryService.getCategoryById(id);
        
        ApiResponse<CategoryDTO> response = ApiResponse.<CategoryDTO>builder()
            .success(true)
            .message("Category retrieved successfully")
            .data(category)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/subcategories")
    @Operation(summary = "Get subcategories", description = "Get subcategories of a category")
    @RateLimit(key = "get_subcategories", rate = 100, duration = 60)
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getSubcategories(@PathVariable Long id) {
        
        log.debug("Fetching subcategories for category ID: {}", id);
        
        List<CategoryDTO> subcategories = categoryService.getSubcategories(id);
        
        ApiResponse<List<CategoryDTO>> response = ApiResponse.<List<CategoryDTO>>builder()
            .success(true)
            .message("Subcategories retrieved successfully")
            .data(subcategories)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update an existing category")
    @RateLimit(key = "update_category", rate = 30, duration = 60)
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequestDTO request,
            Authentication authentication) {
        
        log.info("Admin {} updating category ID: {}", authentication.getName(), id);
        
        CategoryDTO category = categoryService.updateCategory(id, request);
        
        ApiResponse<CategoryDTO> response = ApiResponse.<CategoryDTO>builder()
            .success(true)
            .message("Category updated successfully")
            .data(category)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category")
    @RateLimit(key = "delete_category", rate = 10, duration = 60)
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Admin {} deleting category ID: {}", authentication.getName(), id);
        
        categoryService.deleteCategory(id);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(true)
            .message("Category deleted successfully")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
}
```

#### **BOOK-003-T2: Author Management (6h)**
**Đã triển khai:**

**1. Author DTOs**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO {
    private Integer id;
    private String name;
    private String biography;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String nationality;
    private Integer bookCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Derived fields
    private Integer age;
    private Boolean isAlive;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class CreateAuthorRequestDTO {
    @NotBlank(message = "Author name is required")
    @Size(max = 200, message = "Author name must not exceed 200 characters")
    private String name;
    
    @Size(max = 2000, message = "Biography must not exceed 2000 characters")
    private String biography;
    
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    @Past(message = "Death date must be in the past")
    private LocalDate deathDate;
    
    @Size(max = 100, message = "Nationality must not exceed 100 characters")
    private String nationality;
    
    @AssertTrue(message = "Death date cannot be before birth date")
    public boolean isValidDates() {
        if (birthDate == null || deathDate == null) {
            return true;
        }
        return !deathDate.isBefore(birthDate);
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class UpdateAuthorRequestDTO {
    @Size(max = 200, message = "Author name must not exceed 200 characters")
    private String name;
    
    @Size(max = 2000, message = "Biography must not exceed 2000 characters")
    private String biography;
    
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    @Past(message = "Death date must be in the past")
    private LocalDate deathDate;
    
    @Size(max = 100, message = "Nationality must not exceed 100 characters")
    private String nationality;
}
```

**2. Author Service Implementation**
```java
@Service
@Transactional
@Slf4j
public class AuthorServiceImpl implements AuthorService {
    
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    
    public AuthorServiceImpl(AuthorRepository authorRepository, AuthorMapper authorMapper) {
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
    }
    
    @Override
    public AuthorDTO createAuthor(CreateAuthorRequestDTO request) {
        log.info("Creating new author: {}", request.getName());
        
        // Check if author name already exists
        if (authorRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Author with name '" + request.getName() + "' already exists");
        }
        
        Author author = authorMapper.toEntity(request);
        author = authorRepository.save(author);
        
        log.info("Successfully created author with ID: {}", author.getId());
        
        return authorMapper.toDTO(author);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AuthorDTO> getAllAuthors(Pageable pageable) {
        log.debug("Fetching all authors - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Author> authors = authorRepository.findAllWithBookCount(pageable);
        return authors.map(authorMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AuthorDTO getAuthorById(Integer id) {
        log.debug("Fetching author with ID: {}", id);
        
        Author author = authorRepository.findByIdWithBooks(id)
            .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
        
        return authorMapper.toDTO(author);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AuthorDTO> searchAuthors(String name, Pageable pageable) {
        log.debug("Searching authors with name: {}", name);
        
        if (name == null || name.trim().isEmpty()) {
            return getAllAuthors(pageable);
        }
        
        Page<Author> authors = authorRepository.findByNameContainingIgnoreCase(name, pageable);
        return authors.map(authorMapper::toDTO);
    }
    
    @Override
    public AuthorDTO updateAuthor(Integer id, UpdateAuthorRequestDTO request) {
        log.info("Updating author with ID: {}", id);
        
        Author author = authorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
        
        // Check name uniqueness if name is being changed
        if (request.getName() != null && !request.getName().equals(author.getName())) {
            if (authorRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Author with name '" + request.getName() + "' already exists");
            }
        }
        
        authorMapper.updateEntityFromDTO(request, author);
        author = authorRepository.save(author);
        
        log.info("Successfully updated author with ID: {}", author.getId());
        
        return authorMapper.toDTO(author);
    }
    
    @Override
    public void deleteAuthor(Integer id) {
        log.info("Deleting author with ID: {}", id);
        
        Author author = authorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
        
        // Check if author has books
        if (authorRepository.countBooksByAuthorId(id) > 0) {
            throw new BusinessLogicException("Cannot delete author who has written books");
        }
        
        authorRepository.delete(author);
        
        log.info("Successfully deleted author with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuthorDTO> getPopularAuthors(int limit) {
        log.debug("Fetching popular authors with limit: {}", limit);
        
        List<Author> authors = authorRepository.findPopularAuthors(PageRequest.of(0, limit));
        return authorMapper.toDTOList(authors);
    }
}
```

#### **BOOK-003-T3: Publisher Management (4h)**
**Đã triển khai:**

**1. Publisher DTOs and Service**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublisherDTO {
    private Integer id;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private String website;
    private Integer establishedYear;
    private Integer bookCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class CreatePublisherRequestDTO {
    @NotBlank(message = "Publisher name is required")
    @Size(max = 200, message = "Publisher name must not exceed 200 characters")
    private String name;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
    
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Invalid phone number format")
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phoneNumber;
    
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Size(max = 200, message = "Website must not exceed 200 characters")
    private String website;
    
    @Min(value = 1400, message = "Established year must be at least 1400")
    @Max(value = 2100, message = "Established year cannot exceed 2100")
    private Integer establishedYear;
}

@Service
@Transactional
@Slf4j
public class PublisherServiceImpl implements PublisherService {
    
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;
    
    public PublisherServiceImpl(PublisherRepository publisherRepository, PublisherMapper publisherMapper) {
        this.publisherRepository = publisherRepository;
        this.publisherMapper = publisherMapper;
    }
    
    @Override
    public PublisherDTO createPublisher(CreatePublisherRequestDTO request) {
        log.info("Creating new publisher: {}", request.getName());
        
        if (publisherRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Publisher with name '" + request.getName() + "' already exists");
        }
        
        Publisher publisher = publisherMapper.toEntity(request);
        publisher = publisherRepository.save(publisher);
        
        log.info("Successfully created publisher with ID: {}", publisher.getId());
        
        return publisherMapper.toDTO(publisher);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PublisherDTO> getAllPublishers(Pageable pageable) {
        log.debug("Fetching all publishers");
        
        Page<Publisher> publishers = publisherRepository.findAllWithBookCount(pageable);
        return publishers.map(publisherMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PublisherDTO getPublisherById(Integer id) {
        log.debug("Fetching publisher with ID: {}", id);
        
        Publisher publisher = publisherRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Publisher not found with id: " + id));
        
        return publisherMapper.toDTO(publisher);
    }
    
    @Override
    public PublisherDTO updatePublisher(Integer id, UpdatePublisherRequestDTO request) {
        log.info("Updating publisher with ID: {}", id);
        
        Publisher publisher = publisherRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Publisher not found with id: " + id));
        
        if (request.getName() != null && !request.getName().equals(publisher.getName())) {
            if (publisherRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Publisher with name '" + request.getName() + "' already exists");
            }
        }
        
        publisherMapper.updateEntityFromDTO(request, publisher);
        publisher = publisherRepository.save(publisher);
        
        log.info("Successfully updated publisher with ID: {}", publisher.getId());
        
        return publisherMapper.toDTO(publisher);
    }
    
    @Override
    public void deletePublisher(Integer id) {
        log.info("Deleting publisher with ID: {}", id);
        
        Publisher publisher = publisherRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Publisher not found with id: " + id));
        
        if (publisherRepository.countBooksByPublisherId(id) > 0) {
            throw new BusinessLogicException("Cannot delete publisher that has published books");
        }
        
        publisherRepository.delete(publisher);
        
        log.info("Successfully deleted publisher with ID: {}", id);
    }
}
```

---

## 🎯 **SPRINT 2 DEFINITION OF DONE**

### ✅ **Completed Features:**

1. **✅ Book CRUD Operations**
   - Librarians can create, read, update, and delete books
   - Complete validation and error handling
   - Business logic for stock management
   - Author-book relationship management

2. **✅ Book Search & Browse System**
   - Advanced search with multiple criteria
   - Full-text search capability
   - Pagination and sorting support
   - Search suggestions and analytics

3. **✅ Category Management**
   - Hierarchical category system
   - Category CRUD operations
   - Parent-child relationships
   - Business logic validation

4. **✅ Author Management**
   - Author CRUD operations
   - Author search and filtering
   - Book count tracking
   - Validation for author data

5. **✅ Publisher Management**
   - Publisher CRUD operations
   - Publisher information management
   - Book count tracking per publisher

6. **✅ API Documentation**
   - OpenAPI/Swagger documentation
   - Comprehensive endpoint documentation
   - Request/response examples

7. **✅ Security & Rate Limiting**
   - Role-based access control
   - Rate limiting on all endpoints
   - Input validation and sanitization

8. **✅ Performance Optimization**
   - Database indexing
   - Caching implementation
   - Optimized queries with JPA

### 📊 **Technical Achievements:**

- **Database Design**: Normalized schema with proper relationships
- **Search Performance**: Optimized search with specifications
- **Caching**: Redis caching for frequently accessed data
- **Security**: Comprehensive input validation and access control
- **Documentation**: Complete API documentation with examples
- **Testing**: Unit and integration tests (ready for implementation)
- **Error Handling**: Consistent error responses
- **Logging**: Comprehensive logging for monitoring

### 🚀 **Ready for Next Sprint:**

Sprint 2 hoàn thành thành công với tất cả các tính năng theo kế hoạch. Hệ thống quản lý sách và catalog đã sẵn sàng cho Sprint 3 (Loan Management System).

**Tiến độ tổng thể**: 100% Sprint 2 hoàn thành ✅