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
public class AdminOrderDetailDTO {

    private Long id;
    private String orderCode;
    private LocalDateTime orderDate;
    
    // User information
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userPhoneNumber;
    
    // Financial breakdown
    private BigDecimal subTotalAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    
    // Status information
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private String paymentTransactionId;
    
    // Shipping information
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCity;
    private String shippingPostalCode;
    private String shippingCountry;
    private LocalDateTime shippingDate;
    private LocalDateTime deliveryDate;
    
    // Order items
    private List<AdminOrderItemDTO> orderItems;
    
    // Notes
    private String customerNote;
    private String adminNotes;
    
    // Admin actions
    private List<String> availableActions;
    private boolean canUpdateStatus;
    private boolean canRefund;
    private boolean canCancel;
    
    // Timeline
    private List<OrderTimelineEventDTO> timeline;
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminOrderItemDTO {
        private Long id;
        private Long bookId;
        private String bookTitle;
        private String bookIsbn;
        private Integer quantity;
        private BigDecimal pricePerUnit;
        private BigDecimal itemTotalPrice;
        private Integer currentStock;
        private boolean bookStillExists;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderTimelineEventDTO {
        private String event;
        private String description;
        private LocalDateTime timestamp;
        private String performedBy;
    }
}