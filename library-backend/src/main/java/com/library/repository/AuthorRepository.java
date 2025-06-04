package com.library.repository;

import com.library.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {
    
    List<Author> findByNameContainingIgnoreCase(String name);
    
    Page<Author> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    List<Author> findByNationality(String nationality);
    
    @Query("SELECT DISTINCT a FROM Author a LEFT JOIN FETCH a.bookAuthors ba LEFT JOIN FETCH ba.book WHERE a.id = :id")
    Optional<Author> findByIdWithBooks(@Param("id") Integer id);
    
    @Query("SELECT a FROM Author a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.biography) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Author> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT COUNT(ba) FROM BookAuthor ba WHERE ba.author.id = :authorId")
    Long countBooksByAuthorId(@Param("authorId") Integer authorId);
    
    @Query("SELECT DISTINCT a.nationality FROM Author a WHERE a.nationality IS NOT NULL")
    List<String> findAllNationalities();
    
    @Query("SELECT a FROM Author a JOIN a.bookAuthors ba GROUP BY a ORDER BY COUNT(ba) DESC")
    List<Author> findMostProlificAuthors(Pageable pageable);
}