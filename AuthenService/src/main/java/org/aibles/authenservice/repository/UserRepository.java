package org.aibles.authenservice.repository;

import org.aibles.authenservice.constan.Gender;
import org.aibles.authenservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    
    // Admin queries
    @Query("SELECT u FROM User u WHERE u.email LIKE %:keyword% OR u.name LIKE %:keyword%")
    Page<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    List<User> findByGender(Gender gender);
    
    @Query("SELECT COUNT(u) FROM User u")
    Long countTotalUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.id IN (SELECT a.userId FROM Account a WHERE a.isActivated = true)")
    Long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.id IN (SELECT a.userId FROM Account a WHERE a.isActivated = false)")
    Long countInactiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.id IN (SELECT a.userId FROM Account a WHERE a.isLocked = true)")
    Long countLockedUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.id IN " +
           "(SELECT a.userId FROM Account a WHERE a.id IN " +
           "(SELECT ar.accountId FROM AccountRole ar GROUP BY ar.accountId HAVING COUNT(ar.roleId) > 1))")
    Long countUsersWithMultipleRoles();
}