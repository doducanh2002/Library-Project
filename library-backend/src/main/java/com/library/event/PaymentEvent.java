package com.library.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PaymentEvent extends ApplicationEvent {
    
    public enum PaymentEventType {
        PAYMENT_INITIATED,
        PAYMENT_SUCCESS,
        PAYMENT_FAILED,
        PAYMENT_CANCELLED,
        PAYMENT_REFUNDED,
        PAYMENT_TIMEOUT
    }
    
    private final PaymentEventType eventType;
    private final String userId;
    private final Long paymentId;
    private final Long orderId;
    private final BigDecimal amount;
    private final String currency;
    private final String paymentMethod;
    private final String transactionId;
    private final String reason;
    private final LocalDateTime timestamp;

    public PaymentEvent(Object source, PaymentEventType eventType, String userId, Long paymentId, 
                       Long orderId, BigDecimal amount, String currency) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = null;
        this.transactionId = null;
        this.reason = null;
        this.timestamp = LocalDateTime.now();
    }

    public PaymentEvent(Object source, PaymentEventType eventType, String userId, Long paymentId, 
                       Long orderId, BigDecimal amount, String currency, String paymentMethod, String transactionId) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.reason = null;
        this.timestamp = LocalDateTime.now();
    }

    public PaymentEvent(Object source, PaymentEventType eventType, String userId, Long paymentId, 
                       Long orderId, BigDecimal amount, String currency, String reason) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = null;
        this.transactionId = null;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }
}