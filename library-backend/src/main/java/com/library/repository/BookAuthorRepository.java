package com.library.repository;

import com.library.entity.BookAuthor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookAuthorRepository extends JpaRepository<BookAuthor, BookAuthor.BookAuthorId> {
    
    List<BookAuthor> findByBookId(Long bookId);
    
    List<BookAuthor> findByAuthorId(Long authorId);
    
    Optional<BookAuthor> findByBookIdAndAuthorId(Long bookId, Long authorId);
    
    void deleteByBookId(Long bookId);
    
    void deleteByAuthorId(Long authorId);
    
    void deleteByBookIdAndAuthorId(Long bookId, Long authorId);
    
    @Query("SELECT ba FROM BookAuthor ba WHERE ba.authorRole = :role")
    List<BookAuthor> findByAuthorRole(@Param("role") String role);
    
    @Query("SELECT COUNT(ba) FROM BookAuthor ba WHERE ba.book.id = :bookId")
    Long countAuthorsByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT COUNT(ba) FROM BookAuthor ba WHERE ba.author.id = :authorId")
    Long countBooksByAuthorId(@Param("authorId") Long authorId);
}