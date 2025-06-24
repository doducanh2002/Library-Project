package com.library.repository;

import com.library.entity.Notification;
import com.library.entity.enums.NotificationStatus;
import com.library.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find notifications by user
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    // Find notifications by user and status
    Page<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(
            String userId, NotificationStatus status, Pageable pageable);

    // Find unread notifications count
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = 'UNREAD'")
    Long countUnreadNotifications(@Param("userId") String userId);

    // Find notifications by type
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, NotificationType type);

    // Find high priority notifications
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.priority >= 3 AND n.status = 'UNREAD' ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findHighPriorityUnreadNotifications(@Param("userId") String userId);

    // Find expired notifications
    @Query("SELECT n FROM Notification n WHERE n.expiresAt < :now")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);

    // Find notifications for cleanup (older than specified date)
    @Query("SELECT n FROM Notification n WHERE n.createdAt < :cutoffDate AND n.status != 'UNREAD'")
    List<Notification> findNotificationsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Mark notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :readAt WHERE n.id = :id AND n.userId = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") String userId, @Param("readAt") LocalDateTime readAt);

    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :readAt WHERE n.userId = :userId AND n.status = 'UNREAD'")
    int markAllAsRead(@Param("userId") String userId, @Param("readAt") LocalDateTime readAt);

    // Delete notification by user
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.id = :id AND n.userId = :userId")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") String userId);

    // Find notification by id and user (for security)
    Optional<Notification> findByIdAndUserId(Long id, String userId);

    // Find notifications by reference
    List<Notification> findByReferenceIdAndReferenceType(Long referenceId, String referenceType);

    // Find recent notifications (last 24 hours)
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt > :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("userId") String userId, @Param("since") LocalDateTime since);

    // Find notifications that need email sending
    @Query("SELECT n FROM Notification n WHERE n.isEmailSent = false AND n.priority >= :minPriority")
    List<Notification> findNotificationsNeedingEmail(@Param("minPriority") Integer minPriority);

    // Statistics queries
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId")
    Long countTotalNotifications(@Param("userId") String userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = :status")
    Long countNotificationsByStatus(@Param("userId") String userId, @Param("status") NotificationStatus status);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.type = :type")
    Long countNotificationsByType(@Param("userId") String userId, @Param("type") NotificationType type);

    // Find notifications by multiple criteria
    @Query("SELECT n FROM Notification n WHERE " +
           "(:userId IS NULL OR n.userId = :userId) AND " +
           "(:status IS NULL OR n.status = :status) AND " +
           "(:type IS NULL OR n.type = :type) AND " +
           "(:priority IS NULL OR n.priority = :priority) AND " +
           "(:since IS NULL OR n.createdAt >= :since) AND " +
           "(:until IS NULL OR n.createdAt <= :until) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findNotificationsByCriteria(
            @Param("userId") String userId,
            @Param("status") NotificationStatus status,
            @Param("type") NotificationType type,
            @Param("priority") Integer priority,
            @Param("since") LocalDateTime since,
            @Param("until") LocalDateTime until,
            Pageable pageable);
}