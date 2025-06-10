package com.library.repository;

import com.library.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    List<CartItem> findByUserId(Long userId);
    
    Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId);
    
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    
    void deleteByUserIdAndBookId(Long userId, Long bookId);
    
    void deleteByUserId(Long userId);
    
    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.userId = :userId")
    int countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(c.quantity) FROM CartItem c WHERE c.userId = :userId")
    Integer getTotalQuantityByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(c.unitPrice * c.quantity) FROM CartItem c WHERE c.userId = :userId")
    BigDecimal getTotalPriceByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM CartItem c JOIN FETCH c.book WHERE c.userId = :userId ORDER BY c.createdAt DESC")
    List<CartItem> findByUserIdWithBooks(@Param("userId") Long userId);
    
    @Query("SELECT c FROM CartItem c JOIN FETCH c.book b WHERE c.userId = :userId AND b.isSellable = true AND b.stockForSale >= c.quantity")
    List<CartItem> findValidCartItemsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM CartItem c JOIN c.book b WHERE c.userId = :userId AND (b.isSellable = false OR b.stockForSale < c.quantity)")
    List<CartItem> findInvalidCartItemsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM CartItem c WHERE c.userId = :userId AND c.book.id = :bookId")
    Optional<CartItem> findByUserIdAndBookIdWithBook(@Param("userId") Long userId, @Param("bookId") Long bookId);
}