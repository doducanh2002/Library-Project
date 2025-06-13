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
public class OrderTrackingDTO {

    private String orderCode;
    private OrderStatus currentStatus;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    
    // Timeline events
    private List<OrderTrackingEvent> timeline;
    
    // Current step info
    private String currentStepDescription;
    private String nextStepDescription;
    private LocalDateTime estimatedNextUpdate;
    
    // Shipping info
    private String shippingAddress;
    private String trackingNumber;
    private LocalDateTime estimatedDelivery;
    
    // Progress
    private Integer progressPercentage;
    private boolean canBeCancelled;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderTrackingEvent {
        private String status;
        private String description;
        private LocalDateTime timestamp;
        private String location;
        private boolean isCompleted;
    }
}