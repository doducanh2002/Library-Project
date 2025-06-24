package com.library.service;

import com.library.dto.CreateNotificationRequestDTO;
import com.library.dto.NotificationDTO;
import com.library.dto.NotificationSummaryDTO;
import com.library.entity.enums.NotificationStatus;
import com.library.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {

    // Create notifications
    NotificationDTO createNotification(CreateNotificationRequestDTO request);
    NotificationDTO createNotification(String userId, NotificationType type, String title, String message);
    NotificationDTO createNotification(String userId, NotificationType type, String title, String message, 
                                     Long referenceId, String referenceType);
    NotificationDTO createNotificationWithPriority(String userId, NotificationType type, String title, 
                                                  String message, Integer priority);

    // Get notifications
    Page<NotificationDTO> getUserNotifications(String userId, Pageable pageable);
    Page<NotificationDTO> getUserNotificationsByStatus(String userId, NotificationStatus status, Pageable pageable);
    List<NotificationDTO> getRecentNotifications(String userId, int hours);
    List<NotificationDTO> getHighPriorityUnreadNotifications(String userId);
    NotificationDTO getNotificationById(Long id, String userId);

    // Notification management
    NotificationDTO markAsRead(Long id, String userId);
    int markAllAsRead(String userId);
    int markAsRead(List<Long> notificationIds, String userId);
    NotificationDTO markAsArchived(Long id, String userId);
    boolean deleteNotification(Long id, String userId);
    int bulkDeleteNotifications(List<Long> notificationIds, String userId);

    // Statistics
    NotificationSummaryDTO getNotificationSummary(String userId);
    Long getUnreadCount(String userId);

    // System operations
    void cleanupExpiredNotifications();
    void cleanupOldNotifications(int daysOld);
    List<NotificationDTO> getNotificationsNeedingEmail(Integer minPriority);
    void markEmailSent(Long notificationId);

    // Bulk operations for admin
    Page<NotificationDTO> getAllNotifications(Pageable pageable);
    Page<NotificationDTO> searchNotifications(String userId, NotificationStatus status, NotificationType type,
                                            Integer priority, LocalDateTime since, LocalDateTime until, 
                                            Pageable pageable);

    // Notification templates
    void sendLoanApprovedNotification(String userId, Long loanId, String bookTitle);
    void sendLoanRejectedNotification(String userId, Long loanId, String bookTitle, String reason);
    void sendLoanDueSoonNotification(String userId, Long loanId, String bookTitle, LocalDateTime dueDate);
    void sendLoanOverdueNotification(String userId, Long loanId, String bookTitle, int daysOverdue);
    void sendOrderStatusNotification(String userId, Long orderId, String orderCode, String status);
    void sendPaymentNotification(String userId, Long paymentId, String status, String amount);
    void sendDocumentNotification(String userId, Long documentId, String documentTitle, String action);
    void sendSystemMaintenanceNotification(String message, LocalDateTime scheduledTime);
}