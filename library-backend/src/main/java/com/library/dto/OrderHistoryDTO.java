package com.library.dto;

import com.library.entity.OrderStatus;
import com.library.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistoryDTO {

    private Long id;
    private String orderCode;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    
    // Financial summary
    private BigDecimal totalAmount;
    private Integer totalItems;
    private Integer totalBooks;
    
    // Key dates
    private LocalDateTime paymentDate;
    private LocalDateTime shippingDate;
    private LocalDateTime deliveryDate;
    
    // Location info
    private String shippingCity;
    
    // Order progress
    private String statusDescription;
    private Integer progressPercentage;
    private String nextAction;
    
    // Items preview (first few items)
    private List<OrderItemDTO> itemsPreview;
    
    // Metadata
    private LocalDateTime createdAt;
    private boolean canBeCancelled;
    private boolean canBeReordered;
}