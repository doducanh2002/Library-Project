package com.library.dto;

import com.library.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private Long id;
    private String paymentCode;
    private Long orderId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String vnpTxnRef;
    private String vnpTransactionNo;
    private String vnpOrderInfo;
    private String vnpPaymentUrl;
    private PaymentStatus paymentStatus;
    private String gatewayStatus;
    private String gatewayMessage;
    private LocalDateTime expiresAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper flags
    private boolean expired;
    private boolean pending;
    private boolean completed;
    private boolean failed;
}