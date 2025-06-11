package com.library.dto;

import com.library.entity.OrderStatus;
import com.library.entity.PaymentStatus;
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
public class AdminOrderDTO {

    private Long id;
    private String orderCode;
    private LocalDateTime orderDate;
    
    // User information
    private Long userId;
    private String userFullName;
    private String userEmail;
    
    // Financial information
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    
    // Order summary
    private Integer totalItems;
    private Integer totalBooks;
    
    // Shipping information
    private String shippingCity;
    private LocalDateTime shippingDate;
    private LocalDateTime deliveryDate;
    
    // Admin indicators
    private boolean requiresAction;
    private String actionRequired;
    private Integer priority; // 1=High, 2=Medium, 3=Low
    private Integer daysSinceOrder;
    
    // Notes
    private String customerNote;
    private String adminNotes;
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}