package com.library.repository;

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
    
    Page<Book> findByCategoryId(Integer categoryId, Pageable pageable);
    
    Page<Book> findByPublisherId(Integer publisherId, Pageable pageable);
    
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
}