package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardActivityDTO {
    private String id;
    private String type; // USER_REGISTRATION, BOOK_BORROWED, ORDER_PLACED, DOCUMENT_UPLOADED, etc.
    private String title;
    private String description;
    private String userId;
    private String userName;
    private String entityId; // Book ID, Order ID, Document ID, etc.
    private String entityName;
    private LocalDateTime timestamp;
    private String icon;
    private String color; // For UI display
}