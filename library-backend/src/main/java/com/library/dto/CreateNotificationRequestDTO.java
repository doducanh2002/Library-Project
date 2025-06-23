package com.library.dto;

import com.library.entity.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequestDTO {

    @NotBlank(message = "User ID is required")
    @Size(max = 36, message = "User ID must not exceed 36 characters")
    private String userId;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;

    private Long referenceId;

    @Size(max = 50, message = "Reference type must not exceed 50 characters")
    private String referenceType;

    @Size(max = 500, message = "Action URL must not exceed 500 characters")
    private String actionUrl;

    @Builder.Default
    private Integer priority = 1; // 1 = Low, 2 = Medium, 3 = High, 4 = Critical

    @Builder.Default
    private Boolean sendEmail = false;

    private LocalDateTime expiresAt;

    private String metadata;
}