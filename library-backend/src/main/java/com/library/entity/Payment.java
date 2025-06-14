package com.library.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_code", unique = true, nullable = false, length = 50)
    private String paymentCode;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "vnp_txn_ref", unique = true, nullable = false, length = 100)
    private String vnpTxnRef;

    @Column(name = "vnp_transaction_no", length = 100)
    private String vnpTransactionNo;

    @Column(name = "vnp_order_info", columnDefinition = "TEXT")
    private String vnpOrderInfo;

    @Column(name = "vnp_payment_url", columnDefinition = "TEXT")
    private String vnpPaymentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "gateway_status", length = 50)
    private String gatewayStatus;

    @Column(name = "gateway_message", columnDefinition = "TEXT")
    private String gatewayMessage;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isPending() {
        return paymentStatus == PaymentStatus.PENDING;
    }

    public boolean isCompleted() {
        return paymentStatus == PaymentStatus.COMPLETED;
    }

    public boolean isFailed() {
        return paymentStatus == PaymentStatus.FAILED;
    }

    @PrePersist
    private void prePersist() {
        if (paymentCode == null) {
            paymentCode = "PAY_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        }
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusMinutes(15); // Default 15 minutes timeout
        }
    }
}