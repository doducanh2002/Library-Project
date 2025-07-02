package org.aibles.authenservice.repository;

import org.aibles.authenservice.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<Account> findByUserId(String userId);
    
    // Admin queries
    List<Account> findByIsActivated(Boolean isActivated);
    List<Account> findByIsLocked(Boolean isLocked);
    
    @Query("SELECT a FROM Account a WHERE a.username LIKE %:keyword%")
    Page<Account> findByUsernameContaining(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.isActivated = true")
    Long countActivatedAccounts();
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.isLocked = true")
    Long countLockedAccounts();
}