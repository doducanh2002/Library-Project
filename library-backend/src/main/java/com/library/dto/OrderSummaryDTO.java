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
public class OrderSummaryDTO {

    private Long id;
    private String orderCode;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private String shippingCity;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime createdAt;
}