package com.library.entity.enums;

public enum NotificationType {
    LOAN_APPROVED("Loan Request Approved", "Your loan request has been approved"),
    LOAN_REJECTED("Loan Request Rejected", "Your loan request has been rejected"),
    LOAN_DUE_SOON("Loan Due Soon", "Your borrowed book is due soon"),
    LOAN_OVERDUE("Loan Overdue", "Your borrowed book is overdue"),
    LOAN_RETURNED("Book Returned", "Book has been successfully returned"),
    
    ORDER_CONFIRMED("Order Confirmed", "Your order has been confirmed"),
    ORDER_SHIPPED("Order Shipped", "Your order has been shipped"),
    ORDER_DELIVERED("Order Delivered", "Your order has been delivered"),
    ORDER_CANCELLED("Order Cancelled", "Your order has been cancelled"),
    
    PAYMENT_SUCCESS("Payment Successful", "Your payment has been processed successfully"),
    PAYMENT_FAILED("Payment Failed", "Your payment could not be processed"),
    PAYMENT_REFUND("Payment Refunded", "Your payment has been refunded"),
    
    DOCUMENT_UPLOADED("New Document Available", "A new document has been uploaded"),
    DOCUMENT_ACCESS_GRANTED("Document Access Granted", "You now have access to a new document"),
    
    SYSTEM_MAINTENANCE("System Maintenance", "System maintenance notification"),
    ACCOUNT_ACTIVATED("Account Activated", "Your account has been activated"),
    ACCOUNT_DEACTIVATED("Account Deactivated", "Your account has been deactivated"),
    
    GENERAL("General Notification", "General system notification");
    
    private final String title;
    private final String defaultMessage;
    
    NotificationType(String title, String defaultMessage) {
        this.title = title;
        this.defaultMessage = defaultMessage;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
}