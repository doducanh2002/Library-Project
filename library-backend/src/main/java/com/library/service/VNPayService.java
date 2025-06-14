package com.library.service;

import com.library.dto.PaymentCreateRequestDTO;
import com.library.dto.PaymentResponseDTO;
import com.library.dto.VNPayCallbackRequestDTO;
import com.library.entity.Payment;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VNPayService {
    
    /**
     * Create VNPay payment URL for order
     */
    PaymentResponseDTO createPaymentUrl(PaymentCreateRequestDTO request, HttpServletRequest httpRequest);
    
    /**
     * Process VNPay return URL callback
     */
    PaymentResponseDTO processReturnUrl(Map<String, String> vnpParams);
    
    /**
     * Process VNPay IPN (webhook) callback
     */
    PaymentResponseDTO processWebhook(VNPayCallbackRequestDTO callbackRequest);
    
    /**
     * Get payment by transaction reference
     */
    PaymentResponseDTO getPaymentByTxnRef(String vnpTxnRef);
    
    /**
     * Get payment by order ID
     */
    PaymentResponseDTO getPaymentByOrderId(Long orderId);
    
    /**
     * Cancel payment
     */
    boolean cancelPayment(String paymentCode);
    
    /**
     * Refund payment (admin only)
     */
    PaymentResponseDTO refundPayment(String paymentCode, String reason);
    
    /**
     * Check payment status from VNPay
     */
    PaymentResponseDTO checkPaymentStatus(String vnpTxnRef);
    
    /**
     * Handle expired payments
     */
    void processExpiredPayments();
}