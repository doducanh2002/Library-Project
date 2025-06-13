package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrderStatsDTO {

    // Order counts
    private Long totalOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private Long pendingOrders;
    
    // Financial stats
    private BigDecimal totalSpent;
    private BigDecimal averageOrderValue;
    private BigDecimal largestOrderValue;
    
    // Books stats
    private Integer totalBooksPurchased;
    private Integer uniqueBooksOwned;
    
    // Timeline
    private LocalDateTime firstOrderDate;
    private LocalDateTime lastOrderDate;
    
    // Current status
    private Integer activeOrders;
    private boolean canPlaceNewOrder;
    
    // Preferences
    private String favoriteCategory;
    private String preferredPaymentMethod;
}