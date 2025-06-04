package com.library.repository;

import com.library.entity.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
    
    Optional<Publisher> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Publisher> findByNameContainingIgnoreCase(String name);
    
    Page<Publisher> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    @Query("SELECT p FROM Publisher p LEFT JOIN FETCH p.books WHERE p.id = :id")
    Optional<Publisher> findByIdWithBooks(@Param("id") Integer id);
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.publisher.id = :publisherId")
    Long countBooksByPublisherId(@Param("publisherId") Integer publisherId);
    
    @Query("SELECT p FROM Publisher p WHERE p.establishedYear BETWEEN :startYear AND :endYear")
    List<Publisher> findByEstablishedYearBetween(@Param("startYear") Integer startYear, 
                                                  @Param("endYear") Integer endYear);
    
    @Query("SELECT p FROM Publisher p JOIN p.books b GROUP BY p ORDER BY COUNT(b) DESC")
    List<Publisher> findMostActivePublishers(Pageable pageable);
}