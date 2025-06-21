package com.library.repository;

import com.library.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    Optional<Category> findBySlug(String slug);
    
    boolean existsByName(String name);
    
    boolean existsBySlug(String slug);
    
    List<Category> findByIsActiveTrue();
    
    List<Category> findByParentCategoryIsNull();
    
    List<Category> findByParentCategoryId(Long parentId);
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parentCategory WHERE c.id = :id")
    Optional<Category> findByIdWithParent(@Param("id") Long id);
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subcategories WHERE c.id = :id")
    Optional<Category> findByIdWithSubcategories(@Param("id") Long id);
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.category.id = :categoryId")
    Long countBooksByCategoryId(@Param("categoryId") Long categoryId);
    
    // Admin Dashboard and Reports methods
    @Query("SELECT c.id, c.name, COUNT(DISTINCT b.id) as totalBooks, " +
           "COUNT(DISTINCT l.id) as totalLoans, COUNT(DISTINCT oi.id) as totalOrders, " +
           "ROUND(COUNT(DISTINCT l.id) * 100.0 / NULLIF(SUM(COUNT(DISTINCT l.id)) OVER(), 0), 2) as percentage " +
           "FROM Category c " +
           "LEFT JOIN Book b ON b.category.id = c.id " +
           "LEFT JOIN Loan l ON l.book.id = b.id " +
           "LEFT JOIN OrderItem oi ON oi.book.id = b.id " +
           "GROUP BY c.id, c.name " +
           "ORDER BY (COUNT(DISTINCT l.id) + COUNT(DISTINCT oi.id)) DESC")
    List<Object[]> findMostPopularCategories(@Param("limit") int limit);
}