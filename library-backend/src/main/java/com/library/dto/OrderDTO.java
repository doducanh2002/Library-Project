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
public class OrderDTO {

    private Long id;
    private String orderCode;
    private LocalDateTime orderDate;

    // Financial information
    private BigDecimal subTotalAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    // Status
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentMethod;

    // Shipping information
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCity;
    private String shippingPostalCode;
    private String shippingCountry;
    private LocalDateTime shippingDate;
    private LocalDateTime deliveryDate;

    // Notes
    private String customerNote;

    // Items summary
    private Integer totalItems;
    private List<OrderItemDTO> orderItems;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}