package com.library.event.listener;

import com.library.dto.CreateNotificationRequestDTO;
import com.library.event.*;
import com.library.entity.enums.NotificationType;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @EventListener
    @Async("notificationTaskExecutor")
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            log.info("Processing notification event for user: {}, type: {}", event.getUserId(), event.getType());
            
            CreateNotificationRequestDTO request = CreateNotificationRequestDTO.builder()
                    .userId(event.getUserId())
                    .type(event.getType())
                    .title(event.getTitle())
                    .message(event.getMessage())
                    .referenceId(event.getReferenceId())
                    .referenceType(event.getReferenceType())
                    .priority(event.getPriority())
                    .build();
            
            notificationService.createNotification(request);
            log.info("Notification created successfully for user: {}", event.getUserId());
            
        } catch (Exception e) {
            log.error("Failed to process notification event for user: {}", event.getUserId(), e);
        }
    }

    @EventListener
    @Async("eventTaskExecutor")
    public void handleLoanEvent(LoanEvent event) {
        try {
            log.info("Processing loan event: {} for user: {}", event.getEventType(), event.getUserId());
            
            switch (event.getEventType()) {
                case LOAN_APPROVED -> notificationService.sendLoanApprovedNotification(
                        event.getUserId(), event.getLoanId(), event.getBookTitle());
                        
                case LOAN_REJECTED -> notificationService.sendLoanRejectedNotification(
                        event.getUserId(), event.getLoanId(), event.getBookTitle(), 
                        event.getReason() != null ? event.getReason() : "Requirements not met");
                        
                case LOAN_DUE_SOON -> notificationService.sendLoanDueSoonNotification(
                        event.getUserId(), event.getLoanId(), event.getBookTitle(), event.getDueDate());
                        
                case LOAN_OVERDUE -> notificationService.sendLoanOverdueNotification(
                        event.getUserId(), event.getLoanId(), event.getBookTitle(), 
                        event.getDaysOverdue() != null ? event.getDaysOverdue() : 1);
                        
                case LOAN_RETURNED -> {
                    String title = "Book Returned Successfully";
                    String message = String.format("You have successfully returned '%s'. Thank you!", event.getBookTitle());
                    notificationService.createNotification(event.getUserId(), NotificationType.LOAN_RETURNED, title, message);
                }
                
                case LOAN_RENEWED -> {
                    String title = "Loan Renewed";
                    String message = String.format("Your loan for '%s' has been renewed successfully.", event.getBookTitle());
                    notificationService.createNotification(event.getUserId(), NotificationType.LOAN_APPROVED, title, message);
                }
            }
            
            log.info("Loan notification processed successfully for user: {}", event.getUserId());
            
        } catch (Exception e) {
            log.error("Failed to process loan event for user: {}", event.getUserId(), e);
        }
    }

    @EventListener
    @Async("eventTaskExecutor")
    public void handleOrderEvent(OrderEvent event) {
        try {
            log.info("Processing order event: {} for user: {}", event.getEventType(), event.getUserId());
            
            String status = switch (event.getEventType()) {
                case ORDER_CREATED -> "created";
                case ORDER_CONFIRMED -> "confirmed";
                case ORDER_PAYMENT_PENDING -> "payment pending";
                case ORDER_PAID -> "paid";
                case ORDER_SHIPPED -> "shipped";
                case ORDER_DELIVERED -> "delivered";
                case ORDER_CANCELLED -> "cancelled";
                case ORDER_REFUNDED -> "refunded";
            };
            
            switch (event.getEventType()) {
                case ORDER_CREATED -> {
                    String title = "Order Created";
                    String message = String.format("Your order %s has been created successfully. Please proceed with payment.", 
                            event.getOrderCode());
                    notificationService.createNotification(event.getUserId(), NotificationType.ORDER_CONFIRMED, title, message);
                }
                
                case ORDER_CONFIRMED, ORDER_SHIPPED, ORDER_DELIVERED -> 
                    notificationService.sendOrderStatusNotification(
                            event.getUserId(), event.getOrderId(), event.getOrderCode(), status);
                            
                case ORDER_CANCELLED -> {
                    String title = "Order Cancelled";
                    String message = String.format("Your order %s has been cancelled. %s", 
                            event.getOrderCode(), 
                            event.getReason() != null ? "Reason: " + event.getReason() : "");
                    notificationService.createNotification(event.getUserId(), NotificationType.ORDER_CANCELLED, title, message);
                }
                
                case ORDER_REFUNDED -> {
                    String title = "Order Refunded";
                    String message = String.format("Your order %s has been refunded. The amount will be credited to your account.", 
                            event.getOrderCode());
                    notificationService.createNotificationWithPriority(event.getUserId(), NotificationType.PAYMENT_REFUND, title, message, 2);
                }
            }
            
            log.info("Order notification processed successfully for user: {}", event.getUserId());
            
        } catch (Exception e) {
            log.error("Failed to process order event for user: {}", event.getUserId(), e);
        }
    }

    @EventListener
    @Async("eventTaskExecutor")
    public void handlePaymentEvent(PaymentEvent event) {
        try {
            log.info("Processing payment event: {} for user: {}", event.getEventType(), event.getUserId());
            
            String amountStr = String.format("%,.0f %s", event.getAmount().doubleValue(), 
                    event.getCurrency() != null ? event.getCurrency() : "VND");
            
            switch (event.getEventType()) {
                case PAYMENT_SUCCESS -> notificationService.sendPaymentNotification(
                        event.getUserId(), event.getPaymentId(), "success", amountStr);
                        
                case PAYMENT_FAILED -> {
                    String title = "Payment Failed";
                    String message = String.format("Your payment of %s has failed. %s", 
                            amountStr, 
                            event.getReason() != null ? "Reason: " + event.getReason() : "Please try again.");
                    notificationService.createNotificationWithPriority(event.getUserId(), NotificationType.PAYMENT_FAILED, title, message, 3);
                }
                
                case PAYMENT_REFUNDED -> notificationService.sendPaymentNotification(
                        event.getUserId(), event.getPaymentId(), "refunded", amountStr);
                        
                case PAYMENT_TIMEOUT -> {
                    String title = "Payment Timeout";
                    String message = String.format("Your payment of %s has timed out. Please try again.", amountStr);
                    notificationService.createNotificationWithPriority(event.getUserId(), NotificationType.PAYMENT_FAILED, title, message, 2);
                }
                
                case PAYMENT_CANCELLED -> {
                    String title = "Payment Cancelled";
                    String message = String.format("Your payment of %s has been cancelled.", amountStr);
                    notificationService.createNotification(event.getUserId(), NotificationType.GENERAL, title, message);
                }
            }
            
            log.info("Payment notification processed successfully for user: {}", event.getUserId());
            
        } catch (Exception e) {
            log.error("Failed to process payment event for user: {}", event.getUserId(), e);
        }
    }
}