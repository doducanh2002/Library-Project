package com.library.repository;

import com.library.dto.BookDTO;
import com.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    
    Optional<Book> findByIsbn(String isbn);
    
    boolean existsByIsbn(String isbn);
    
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.publisher WHERE b.id = :id")
    Optional<Book> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.bookAuthors ba LEFT JOIN FETCH ba.author WHERE b.id = :id")
    Optional<Book> findByIdWithAuthors(@Param("id") Long id);
    
    List<Book> findByCategoryId(Long categoryId);
    
    List<Book> findByPublisherId(Long publisherId);
    
    List<Book> findByBookAuthors_AuthorId(Long authorId);
    
    boolean existsByIsbnAndIdNot(String isbn, Long id);
    
    @Query("SELECT b FROM Book b WHERE b.isLendable = true AND b.availableCopiesForLoan > 0")
    Page<Book> findAvailableForLoan(Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.isSellable = true AND b.stockForSale > 0")
    Page<Book> findAvailableForSale(Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT b FROM Book b JOIN b.bookAuthors ba JOIN ba.author a WHERE a.id = :authorId")
    Page<Book> findByAuthorId(@Param("authorId") Integer authorId, Pageable pageable);
    
    @Query("SELECT b FROM Book b ORDER BY b.createdAt DESC")
    List<Book> findRecentBooks(Pageable pageable);
    
    @Query("SELECT b FROM Book b LEFT JOIN b.bookAuthors ba GROUP BY b ORDER BY COUNT(ba) DESC")
    List<Book> findPopularBooks(Pageable pageable);
    
    // Full-text search queries with PostgreSQL optimization
    @Query(value = "SELECT * FROM books b WHERE " +
           "to_tsvector('english', COALESCE(b.title, '') || ' ' || COALESCE(b.description, '')) " +
           "@@ plainto_tsquery('english', :searchText) " +
           "ORDER BY ts_rank(to_tsvector('english', COALESCE(b.title, '') || ' ' || COALESCE(b.description, '')), " +
           "plainto_tsquery('english', :searchText)) DESC", 
           countQuery = "SELECT count(*) FROM books b WHERE " +
           "to_tsvector('english', COALESCE(b.title, '') || ' ' || COALESCE(b.description, '')) " +
           "@@ plainto_tsquery('english', :searchText)",
           nativeQuery = true)
    Page<Book> findByFullTextSearch(@Param("searchText") String searchText, Pageable pageable);
    
    // Enhanced keyword search with ranking
    @Query(value = "SELECT * FROM books b WHERE " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY CASE " +
           "WHEN LOWER(b.title) = LOWER(:keyword) THEN 1 " +
           "WHEN LOWER(b.title) LIKE LOWER(CONCAT(:keyword, '%')) THEN 2 " +
           "WHEN LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 3 " +
           "ELSE 4 END",
           nativeQuery = true)
    Page<Book> findByEnhancedKeywordSearch(@Param("keyword") String keyword, Pageable pageable);
    
    // Search by multiple authors with performance optimization
    @Query("SELECT DISTINCT b FROM Book b JOIN b.bookAuthors ba WHERE ba.author.id IN :authorIds")
    Page<Book> findByMultipleAuthors(@Param("authorIds") List<Integer> authorIds, Pageable pageable);
    
    // Search by multiple publishers
    @Query("SELECT b FROM Book b WHERE b.publisher.id IN :publisherIds")
    Page<Book> findByMultiplePublishers(@Param("publisherIds") List<Long> publisherIds, Pageable pageable);
    
    // Search suggestions for autocomplete
    @Query("SELECT DISTINCT b.title FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT(:partialText, '%')) " +
           "ORDER BY LENGTH(b.title) ASC")
    List<String> findTitleSuggestions(@Param("partialText") String partialText, Pageable pageable);
    
    // Author suggestions for autocomplete
    @Query("SELECT DISTINCT a.name FROM Book b JOIN b.bookAuthors ba JOIN ba.author a " +
           "WHERE LOWER(a.name) LIKE LOWER(CONCAT(:partialText, '%')) " +
           "ORDER BY a.name ASC")
    List<String> findAuthorSuggestions(@Param("partialText") String partialText, Pageable pageable);
    
    // Popular keywords based on book titles and descriptions
    @Query(value = "SELECT word FROM ( " +
           "SELECT regexp_split_to_table(LOWER(title), E'\\\\s+') as word FROM books " +
           "UNION ALL " +
           "SELECT regexp_split_to_table(LOWER(description), E'\\\\s+') as word FROM books " +
           ") t WHERE LENGTH(word) > 3 AND word ~ '^[a-zA-Z]+$' " +
           "GROUP BY word ORDER BY COUNT(*) DESC LIMIT :limit",
           nativeQuery = true)
    List<String> findPopularKeywords(@Param("limit") int limit);
    
    // Optimized count query for large datasets
    @Query("SELECT COUNT(b) FROM Book b WHERE b.isLendable = true AND b.availableCopiesForLoan > 0")
    long countAvailableForLoan();
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.isSellable = true AND b.stockForSale > 0")
    long countAvailableForSale();
    
    // Advanced filtering with category hierarchy
    @Query("SELECT DISTINCT b FROM Book b WHERE b.category.id IN :categoryIds " +
           "OR b.category.parentCategory.id IN :categoryIds")
    Page<Book> findByCategoryIdsIncludingParent(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);
    
    // Books with high ratings or popularity (for future use)
    @Query("SELECT b FROM Book b WHERE b.createdAt >= :fromDate ORDER BY b.createdAt DESC")
    Page<Book> findRecentlyAdded(@Param("fromDate") java.time.LocalDateTime fromDate, Pageable pageable);
    
    // Performance optimized search for admin dashboard
    @Query("SELECT new com.library.dto.BookDTO(b.id, b.title, b.isbn, b.price, b.stockForSale, " +
           "b.availableCopiesForLoan, b.isLendable, b.isSellable) FROM Book b")
    Page<BookDTO> findAllForDashboard(Pageable pageable);
    
    // Search with filters for better performance
    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:categoryId IS NULL OR b.category.id = :categoryId) AND " +
           "(:publisherId IS NULL OR b.publisher.id = :publisherId) AND " +
           "(:language IS NULL OR b.language = :language) AND " +
           "(:isLendable IS NULL OR b.isLendable = :isLendable) AND " +
           "(:isSellable IS NULL OR b.isSellable = :isSellable)")
    Page<Book> findWithFilters(
        @Param("title") String title,
        @Param("categoryId") Long categoryId, 
        @Param("publisherId") Long publisherId,
        @Param("language") String language,
        @Param("isLendable") Boolean isLendable,
        @Param("isSellable") Boolean isSellable,
        Pageable pageable);
}