package com.library.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class OrderEvent extends ApplicationEvent {
    
    public enum OrderEventType {
        ORDER_CREATED,
        ORDER_CONFIRMED,
        ORDER_PAYMENT_PENDING,
        ORDER_PAID,
        ORDER_SHIPPED,
        ORDER_DELIVERED,
        ORDER_CANCELLED,
        ORDER_REFUNDED
    }
    
    private final OrderEventType eventType;
    private final String userId;
    private final Long orderId;
    private final String orderCode;
    private final BigDecimal amount;
    private final String reason;
    private final LocalDateTime timestamp;

    public OrderEvent(Object source, OrderEventType eventType, String userId, Long orderId, String orderCode) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.amount = null;
        this.reason = null;
        this.timestamp = LocalDateTime.now();
    }

    public OrderEvent(Object source, OrderEventType eventType, String userId, Long orderId, String orderCode, BigDecimal amount) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.amount = amount;
        this.reason = null;
        this.timestamp = LocalDateTime.now();
    }

    public OrderEvent(Object source, OrderEventType eventType, String userId, Long orderId, String orderCode, String reason) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.amount = null;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }
}