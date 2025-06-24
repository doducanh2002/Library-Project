package com.library.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkNotificationRequestDTO {
    
    @NotEmpty(message = "Notification IDs are required")
    private List<Long> notificationIds;
    
    private String action; // "read", "unread", "archive"
}