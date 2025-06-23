package com.library.dto;

import com.library.entity.enums.NotificationStatus;
import com.library.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private String userId;
    private NotificationType type;
    private String title;
    private String message;
    private NotificationStatus status;
    private Long referenceId;
    private String referenceType;
    private String actionUrl;
    private Integer priority;
    private String priorityLabel;
    private Boolean isEmailSent;
    private LocalDateTime emailSentAt;
    private LocalDateTime readAt;
    private LocalDateTime expiresAt;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isUnread;
    private Boolean isRead;
    private Boolean isExpired;
    private Boolean isHighPriority;
}