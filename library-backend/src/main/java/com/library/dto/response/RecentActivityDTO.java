package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentActivityDTO {
    private String activityType; // LOAN_CREATED, LOAN_RETURNED, ORDER_PLACED, USER_REGISTERED, etc.
    private String description;
    private String userName;
    private String itemName; // book title, document name, etc.
    private LocalDateTime timestamp;
    private String status;
    private String icon;
}