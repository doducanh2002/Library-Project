package com.library.service.impl;

import com.library.dto.CreateNotificationRequestDTO;
import com.library.dto.NotificationDTO;
import com.library.dto.NotificationSummaryDTO;
import com.library.entity.Notification;
import com.library.entity.enums.NotificationStatus;
import com.library.entity.enums.NotificationType;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.NotificationMapper;
import com.library.repository.NotificationRepository;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Caching(evict = {
        @CacheEvict(value = "notifications", key = "#request.userId"),
        @CacheEvict(value = "notification-summary", key = "#request.userId"),
        @CacheEvict(value = "notification-count", key = "#request.userId")
    })
    public NotificationDTO createNotification(CreateNotificationRequestDTO request) {
        log.info("Creating notification for user: {}, type: {}", request.getUserId(), request.getType());
        
        Notification notification = notificationMapper.toEntity(request);
        notification = notificationRepository.save(notification);
        
        log.info("Notification created with ID: {}", notification.getId());
        return notificationMapper.toDTO(notification);
    }

    @Override
    public NotificationDTO createNotification(String userId, NotificationType type, String title, String message) {
        CreateNotificationRequestDTO request = CreateNotificationRequestDTO.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .priority(1)
                .build();
        return createNotification(request);
    }

    @Override
    public NotificationDTO createNotification(String userId, NotificationType type, String title, String message, 
                                            Long referenceId, String referenceType) {
        CreateNotificationRequestDTO request = CreateNotificationRequestDTO.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .priority(2)
                .build();
        return createNotification(request);
    }

    @Override
    public NotificationDTO createNotificationWithPriority(String userId, NotificationType type, String title, 
                                                         String message, Integer priority) {
        CreateNotificationRequestDTO request = CreateNotificationRequestDTO.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .priority(priority)
                .build();
        return createNotification(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(String userId, Pageable pageable) {
        log.debug("Getting notifications for user: {}", userId);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(notificationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotificationsByStatus(String userId, NotificationStatus status, Pageable pageable) {
        log.debug("Getting notifications for user: {} with status: {}", userId, status);
        Page<Notification> notifications = notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        return notifications.map(notificationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getRecentNotifications(String userId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<Notification> notifications = notificationRepository.findRecentNotifications(userId, since);
        return notificationMapper.toDTOList(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getHighPriorityUnreadNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findHighPriorityUnreadNotifications(userId);
        return notificationMapper.toDTOList(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(Long id, String userId) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        return notificationMapper.toDTO(notification);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "notification-summary", key = "#userId"),
        @CacheEvict(value = "notification-count", key = "#userId")
    })
    public NotificationDTO markAsRead(Long id, String userId) {
        log.info("Marking notification as read: {} for user: {}", id, userId);
        
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        
        if (!notification.isRead()) {
            notification.markAsRead();
            notification = notificationRepository.save(notification);
            log.info("Notification {} marked as read", id);
        }
        
        return notificationMapper.toDTO(notification);
    }

    @Override
    public int markAllAsRead(String userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        LocalDateTime now = LocalDateTime.now();
        int updated = notificationRepository.markAllAsRead(userId, now);
        log.info("Marked {} notifications as read for user: {}", updated, userId);
        return updated;
    }

    @Override
    public int markAsRead(List<Long> notificationIds, String userId) {
        log.info("Marking {} notifications as read for user: {}", notificationIds.size(), userId);
        
        int totalUpdated = 0;
        LocalDateTime now = LocalDateTime.now();
        
        for (Long id : notificationIds) {
            int updated = notificationRepository.markAsRead(id, userId, now);
            totalUpdated += updated;
        }
        
        log.info("Marked {} notifications as read for user: {}", totalUpdated, userId);
        return totalUpdated;
    }

    @Override
    public NotificationDTO markAsArchived(Long id, String userId) {
        log.info("Archiving notification: {} for user: {}", id, userId);
        
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        
        notification.markAsArchived();
        notification = notificationRepository.save(notification);
        
        return notificationMapper.toDTO(notification);
    }

    @Override
    public boolean deleteNotification(Long id, String userId) {
        log.info("Deleting notification: {} for user: {}", id, userId);
        
        int deleted = notificationRepository.deleteByIdAndUserId(id, userId);
        boolean success = deleted > 0;
        
        if (success) {
            log.info("Notification {} deleted successfully", id);
        } else {
            log.warn("Notification {} not found or not owned by user {}", id, userId);
        }
        
        return success;
    }

    @Override
    public int bulkDeleteNotifications(List<Long> notificationIds, String userId) {
        log.info("Bulk deleting {} notifications for user: {}", notificationIds.size(), userId);
        
        int totalDeleted = 0;
        for (Long id : notificationIds) {
            int deleted = notificationRepository.deleteByIdAndUserId(id, userId);
            totalDeleted += deleted;
        }
        
        log.info("Deleted {} notifications for user: {}", totalDeleted, userId);
        return totalDeleted;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "notification-summary", key = "#userId")
    public NotificationSummaryDTO getNotificationSummary(String userId) {
        log.debug("Getting notification summary for user: {}", userId);
        
        Long total = notificationRepository.countTotalNotifications(userId);
        Long unread = notificationRepository.countUnreadNotifications(userId);
        Long read = notificationRepository.countNotificationsByStatus(userId, NotificationStatus.READ);
        Long archived = notificationRepository.countNotificationsByStatus(userId, NotificationStatus.ARCHIVED);
        Long highPriorityUnread = (long) notificationRepository.findHighPriorityUnreadNotifications(userId).size();
        Long expired = (long) notificationRepository.findExpiredNotifications(LocalDateTime.now()).size();
        
        return NotificationSummaryDTO.builder()
                .totalNotifications(total)
                .unreadCount(unread)
                .readCount(read)
                .archivedCount(archived)
                .highPriorityUnreadCount(highPriorityUnread)
                .expiredCount(expired)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "notification-count", key = "#userId")
    public Long getUnreadCount(String userId) {
        return notificationRepository.countUnreadNotifications(userId);
    }

    @Override
    public void cleanupExpiredNotifications() {
        log.info("Cleaning up expired notifications");
        List<Notification> expiredNotifications = notificationRepository.findExpiredNotifications(LocalDateTime.now());
        
        for (Notification notification : expiredNotifications) {
            notification.markAsArchived();
        }
        
        if (!expiredNotifications.isEmpty()) {
            notificationRepository.saveAll(expiredNotifications);
            log.info("Archived {} expired notifications", expiredNotifications.size());
        }
    }

    @Override
    public void cleanupOldNotifications(int daysOld) {
        log.info("Cleaning up notifications older than {} days", daysOld);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<Notification> oldNotifications = notificationRepository.findNotificationsForCleanup(cutoffDate);
        
        if (!oldNotifications.isEmpty()) {
            notificationRepository.deleteAll(oldNotifications);
            log.info("Deleted {} old notifications", oldNotifications.size());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsNeedingEmail(Integer minPriority) {
        List<Notification> notifications = notificationRepository.findNotificationsNeedingEmail(minPriority);
        return notificationMapper.toDTOList(notifications);
    }

    @Override
    public void markEmailSent(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsEmailSent(true);
            notification.setEmailSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getAllNotifications(Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findAll(pageable);
        return notifications.map(notificationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> searchNotifications(String userId, NotificationStatus status, NotificationType type,
                                                   Integer priority, LocalDateTime since, LocalDateTime until, 
                                                   Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findNotificationsByCriteria(
                userId, status, type, priority, since, until, pageable);
        return notifications.map(notificationMapper::toDTO);
    }

    // Notification templates implementation
    @Override
    public void sendLoanApprovedNotification(String userId, Long loanId, String bookTitle) {
        String title = "Loan Request Approved";
        String message = String.format("Your loan request for '%s' has been approved. You can now pick up the book.", bookTitle);
        createNotification(userId, NotificationType.LOAN_APPROVED, title, message, loanId, "LOAN");
    }

    @Override
    public void sendLoanRejectedNotification(String userId, Long loanId, String bookTitle, String reason) {
        String title = "Loan Request Rejected";
        String message = String.format("Your loan request for '%s' has been rejected. Reason: %s", bookTitle, reason);
        createNotification(userId, NotificationType.LOAN_REJECTED, title, message, loanId, "LOAN");
    }

    @Override
    public void sendLoanDueSoonNotification(String userId, Long loanId, String bookTitle, LocalDateTime dueDate) {
        String title = "Book Due Soon";
        String message = String.format("The book '%s' is due on %s. Please return it on time to avoid late fees.", 
                bookTitle, dueDate.toLocalDate());
        createNotificationWithPriority(userId, NotificationType.LOAN_DUE_SOON, title, message, 2);
    }

    @Override
    public void sendLoanOverdueNotification(String userId, Long loanId, String bookTitle, int daysOverdue) {
        String title = "Book Overdue";
        String message = String.format("The book '%s' is %d days overdue. Please return it immediately to avoid additional fees.", 
                bookTitle, daysOverdue);
        createNotificationWithPriority(userId, NotificationType.LOAN_OVERDUE, title, message, 3);
    }

    @Override
    public void sendOrderStatusNotification(String userId, Long orderId, String orderCode, String status) {
        String title = String.format("Order %s", status);
        String message = String.format("Your order %s has been %s.", orderCode, status.toLowerCase());
        NotificationType type = switch (status.toUpperCase()) {
            case "CONFIRMED" -> NotificationType.ORDER_CONFIRMED;
            case "SHIPPED" -> NotificationType.ORDER_SHIPPED;
            case "DELIVERED" -> NotificationType.ORDER_DELIVERED;
            case "CANCELLED" -> NotificationType.ORDER_CANCELLED;
            default -> NotificationType.GENERAL;
        };
        createNotification(userId, type, title, message, orderId, "ORDER");
    }

    @Override
    public void sendPaymentNotification(String userId, Long paymentId, String status, String amount) {
        String title = String.format("Payment %s", status);
        String message = String.format("Your payment of %s has been %s.", amount, status.toLowerCase());
        NotificationType type = switch (status.toUpperCase()) {
            case "SUCCESS", "COMPLETED" -> NotificationType.PAYMENT_SUCCESS;
            case "FAILED" -> NotificationType.PAYMENT_FAILED;
            case "REFUNDED" -> NotificationType.PAYMENT_REFUND;
            default -> NotificationType.GENERAL;
        };
        createNotification(userId, type, title, message, paymentId, "PAYMENT");
    }

    @Override
    public void sendDocumentNotification(String userId, Long documentId, String documentTitle, String action) {
        String title = String.format("Document %s", action);
        String message = String.format("The document '%s' has been %s.", documentTitle, action.toLowerCase());
        NotificationType type = "UPLOADED".equals(action.toUpperCase()) ? 
                NotificationType.DOCUMENT_UPLOADED : NotificationType.DOCUMENT_ACCESS_GRANTED;
        createNotification(userId, type, title, message, documentId, "DOCUMENT");
    }

    @Override
    public void sendSystemMaintenanceNotification(String message, LocalDateTime scheduledTime) {
        // This would typically send to all users or specific user groups
        // For now, we'll create a notification for system admin
        String title = "System Maintenance Scheduled";
        String fullMessage = String.format("%s Scheduled for: %s", message, scheduledTime);
        // This would need to be implemented to send to multiple users
        log.info("System maintenance notification: {}", fullMessage);
    }
}