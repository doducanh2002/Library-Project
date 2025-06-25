package com.library.entity;

import com.library.entity.enums.NotificationStatus;
import com.library.entity.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user_id", columnList = "user_id"),
    @Index(name = "idx_notification_status", columnList = "status"),
    @Index(name = "idx_notification_type", columnList = "type"),
    @Index(name = "idx_notification_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "User ID is required")
    @Size(max = 36, message = "User ID must not exceed 36 characters")
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.UNREAD;

    @Column(name = "reference_id")
    private Long referenceId;

    @Size(max = 50, message = "Reference type must not exceed 50 characters")
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 1; // 1 = Low, 2 = Medium, 3 = High, 4 = Critical

    @Column(name = "is_email_sent", nullable = false)
    @Builder.Default
    private Boolean isEmailSent = false;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void markAsRead() {
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }

    public void markAsArchived() {
        this.status = NotificationStatus.ARCHIVED;
    }

    public boolean isUnread() {
        return NotificationStatus.UNREAD.equals(this.status);
    }

    public boolean isRead() {
        return NotificationStatus.READ.equals(this.status);
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isHighPriority() {
        return priority != null && priority >= 3;
    }

    public String getPriorityLabel() {
        if (priority == null) return "Low";
        return switch (priority) {
            case 1 -> "Low";
            case 2 -> "Medium";
            case 3 -> "High";
            case 4 -> "Critical";
            default -> "Unknown";
        };
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", createdAt=" + createdAt +
                '}';
    }
}