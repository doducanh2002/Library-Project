package com.library.service;

import com.library.dto.CreateNotificationRequestDTO;
import com.library.dto.NotificationDTO;
import com.library.dto.NotificationSummaryDTO;
import com.library.entity.Notification;
import com.library.entity.enums.NotificationStatus;
import com.library.entity.enums.NotificationType;
import com.library.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    private final String TEST_USER_ID = "test-user-123";
    private final String ANOTHER_USER_ID = "another-user-456";

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    @Test
    void createNotification_ShouldCreateSuccessfully() {
        // Given
        CreateNotificationRequestDTO request = CreateNotificationRequestDTO.builder()
                .userId(TEST_USER_ID)
                .type(NotificationType.LOAN_APPROVED)
                .title("Test Notification")
                .message("This is a test notification")
                .priority(2)
                .build();

        // When
        NotificationDTO result = notificationService.createNotification(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getType()).isEqualTo(NotificationType.LOAN_APPROVED);
        assertThat(result.getTitle()).isEqualTo("Test Notification");
        assertThat(result.getMessage()).isEqualTo("This is a test notification");
        assertThat(result.getPriority()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(result.getIsUnread()).isTrue();
        assertThat(result.getIsRead()).isFalse();
    }

    @Test
    void getUserNotifications_ShouldReturnPaginatedResults() {
        // Given
        createTestNotifications();

        // When
        Page<NotificationDTO> result = notificationService.getUserNotifications(
                TEST_USER_ID, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Notification 3"); // Most recent first
    }

    @Test
    void getUserNotificationsByStatus_ShouldFilterCorrectly() {
        // Given
        createTestNotifications();

        // When
        Page<NotificationDTO> result = notificationService.getUserNotificationsByStatus(
                TEST_USER_ID, NotificationStatus.UNREAD, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).allMatch(notification -> notification.getStatus() == NotificationStatus.UNREAD);
    }

    @Test
    void markAsRead_ShouldUpdateNotificationStatus() {
        // Given
        NotificationDTO notification = createSingleNotification();

        // When
        NotificationDTO result = notificationService.markAsRead(notification.getId(), TEST_USER_ID);

        // Then
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(result.getIsRead()).isTrue();
        assertThat(result.getIsUnread()).isFalse();
        assertThat(result.getReadAt()).isNotNull();
    }

    @Test
    void markAllAsRead_ShouldUpdateAllUserNotifications() {
        // Given
        createTestNotifications();

        // When
        int updated = notificationService.markAllAsRead(TEST_USER_ID);

        // Then
        assertThat(updated).isEqualTo(3);
        
        Page<NotificationDTO> notifications = notificationService.getUserNotifications(
                TEST_USER_ID, PageRequest.of(0, 10));
        assertThat(notifications.getContent()).allMatch(notification -> notification.getStatus() == NotificationStatus.READ);
    }

    @Test
    void getNotificationSummary_ShouldReturnCorrectCounts() {
        // Given
        createTestNotifications();
        
        // Mark one as read
        Page<NotificationDTO> notifications = notificationService.getUserNotifications(
                TEST_USER_ID, PageRequest.of(0, 1));
        notificationService.markAsRead(notifications.getContent().get(0).getId(), TEST_USER_ID);

        // When
        NotificationSummaryDTO summary = notificationService.getNotificationSummary(TEST_USER_ID);

        // Then
        assertThat(summary.getTotalNotifications()).isEqualTo(3);
        assertThat(summary.getUnreadCount()).isEqualTo(2);
        assertThat(summary.getReadCount()).isEqualTo(1);
        assertThat(summary.getArchivedCount()).isEqualTo(0);
    }

    @Test
    void getHighPriorityUnreadNotifications_ShouldFilterCorrectly() {
        // Given
        createNotificationWithPriority(NotificationType.LOAN_OVERDUE, "High Priority", 3);
        createNotificationWithPriority(NotificationType.GENERAL, "Low Priority", 1);
        createNotificationWithPriority(NotificationType.PAYMENT_FAILED, "Critical", 4);

        // When
        List<NotificationDTO> highPriorityNotifications = 
                notificationService.getHighPriorityUnreadNotifications(TEST_USER_ID);

        // Then
        assertThat(highPriorityNotifications).hasSize(2);
        assertThat(highPriorityNotifications).allMatch(notification -> notification.getPriority() >= 3);
    }

    @Test
    void deleteNotification_ShouldRemoveFromDatabase() {
        // Given
        NotificationDTO notification = createSingleNotification();

        // When
        boolean deleted = notificationService.deleteNotification(notification.getId(), TEST_USER_ID);

        // Then
        assertThat(deleted).isTrue();
        assertThat(notificationRepository.findById(notification.getId())).isEmpty();
    }

    @Test
    void bulkMarkAsRead_ShouldUpdateMultipleNotifications() {
        // Given
        createTestNotifications();
        Page<NotificationDTO> notifications = notificationService.getUserNotifications(
                TEST_USER_ID, PageRequest.of(0, 2));
        List<Long> notificationIds = notifications.getContent().stream()
                .map(NotificationDTO::getId)
                .toList();

        // When
        int updated = notificationService.markAsRead(notificationIds, TEST_USER_ID);

        // Then
        assertThat(updated).isEqualTo(2);
        
        for (Long id : notificationIds) {
            NotificationDTO notification = notificationService.getNotificationById(id, TEST_USER_ID);
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.READ);
        }
    }

    @Test
    void getRecentNotifications_ShouldFilterByTime() {
        // Given
        createTestNotifications();
        
        // Create an old notification
        Notification oldNotification = Notification.builder()
                .userId(TEST_USER_ID)
                .type(NotificationType.GENERAL)
                .title("Old Notification")
                .message("This is old")
                .priority(1)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        notificationRepository.save(oldNotification);

        // When
        List<NotificationDTO> recentNotifications = notificationService.getRecentNotifications(TEST_USER_ID, 24);

        // Then
        assertThat(recentNotifications).hasSize(3); // Only recent ones
        assertThat(recentNotifications).noneMatch(notification -> notification.getTitle().equals("Old Notification"));
    }

    @Test
    void sendLoanNotifications_ShouldCreateAppropriateNotifications() {
        // When
        notificationService.sendLoanApprovedNotification(TEST_USER_ID, 1L, "Test Book");
        notificationService.sendLoanRejectedNotification(TEST_USER_ID, 2L, "Another Book", "Insufficient copies");
        notificationService.sendLoanDueSoonNotification(TEST_USER_ID, 3L, "Due Book", LocalDateTime.now().plusDays(1));
        notificationService.sendLoanOverdueNotification(TEST_USER_ID, 4L, "Overdue Book", 3);

        // Then
        Page<NotificationDTO> notifications = notificationService.getUserNotifications(
                TEST_USER_ID, PageRequest.of(0, 10));
        
        assertThat(notifications.getContent()).hasSize(4);
        assertThat(notifications.getContent()).extracting(NotificationDTO::getType)
                .containsExactlyInAnyOrder(
                        NotificationType.LOAN_APPROVED,
                        NotificationType.LOAN_REJECTED,
                        NotificationType.LOAN_DUE_SOON,
                        NotificationType.LOAN_OVERDUE
                );
    }

    @Test
    void cleanupExpiredNotifications_ShouldArchiveExpiredOnes() {
        // Given
        Notification expiredNotification = Notification.builder()
                .userId(TEST_USER_ID)
                .type(NotificationType.GENERAL)
                .title("Expired Notification")
                .message("This should be archived")
                .priority(1)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
        notificationRepository.save(expiredNotification);

        createSingleNotification(); // Non-expired notification

        // When
        notificationService.cleanupExpiredNotifications();

        // Then
        List<Notification> allNotifications = notificationRepository.findAll();
        assertThat(allNotifications).hasSize(2);
        
        Notification archived = allNotifications.stream()
                .filter(n -> n.getTitle().equals("Expired Notification"))
                .findFirst()
                .orElseThrow();
        
        assertThat(archived.getStatus()).isEqualTo(NotificationStatus.ARCHIVED);
    }

    private void createTestNotifications() {
        for (int i = 1; i <= 3; i++) {
            CreateNotificationRequestDTO request = CreateNotificationRequestDTO.builder()
                    .userId(TEST_USER_ID)
                    .type(NotificationType.GENERAL)
                    .title("Notification " + i)
                    .message("Message " + i)
                    .priority(1)
                    .build();
            notificationService.createNotification(request);
        }
    }

    private NotificationDTO createSingleNotification() {
        CreateNotificationRequestDTO request = CreateNotificationRequestDTO.builder()
                .userId(TEST_USER_ID)
                .type(NotificationType.GENERAL)
                .title("Single Notification")
                .message("Single message")
                .priority(1)
                .build();
        return notificationService.createNotification(request);
    }

    private void createNotificationWithPriority(NotificationType type, String title, Integer priority) {
        CreateNotificationRequestDTO request = CreateNotificationRequestDTO.builder()
                .userId(TEST_USER_ID)
                .type(type)
                .title(title)
                .message("Test message")
                .priority(priority)
                .build();
        notificationService.createNotification(request);
    }
}