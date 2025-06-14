package com.library.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.config.VNPayConfig;
import com.library.dto.PaymentCreateRequestDTO;
import com.library.dto.PaymentResponseDTO;
import com.library.dto.VNPayCallbackRequestDTO;
import com.library.entity.*;
import com.library.repository.OrderRepository;
import com.library.repository.PaymentRepository;
import com.library.repository.PaymentTransactionRepository;
import com.library.service.VNPayService;
import com.library.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PaymentResponseDTO createPaymentUrl(PaymentCreateRequestDTO request, HttpServletRequest httpRequest) {
        log.info("Creating VNPay payment URL for order: {}", request.getOrderId());
        
        try {
            // Validate order
            Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + request.getOrderId()));
            
            if (!OrderStatus.PENDING_PAYMENT.equals(order.getStatus())) {
                throw new RuntimeException("Order is not in pending payment status");
            }
            
            // Check if payment already exists for this order
            if (paymentRepository.existsByOrderIdAndPaymentStatus(request.getOrderId(), PaymentStatus.PENDING)) {
                throw new RuntimeException("Pending payment already exists for this order");
            }
            
            // Create payment record
            String vnpTxnRef = generateTxnRef(order.getId());
            String orderInfo = "Thanh toan don hang #" + order.getOrderCode();
            
            Payment payment = Payment.builder()
                .orderId(order.getId())
                .amount(order.getTotalAmount())
                .currency("VND")
                .paymentMethod(request.getPaymentMethod())
                .vnpTxnRef(vnpTxnRef)
                .vnpOrderInfo(orderInfo)
                .paymentStatus(PaymentStatus.PENDING)
                .ipAddress(request.getIpAddress() != null ? request.getIpAddress() : VNPayUtil.getIpAddress(httpRequest))
                .userAgent(request.getUserAgent() != null ? request.getUserAgent() : httpRequest.getHeader("User-Agent"))
                .expiresAt(LocalDateTime.now().plusMinutes(vnPayConfig.getTimeoutMinutes()))
                .build();
                
            payment = paymentRepository.save(payment);
            
            // Generate VNPay URL
            String paymentUrl = buildVNPayUrl(payment, request);
            payment.setVnpPaymentUrl(paymentUrl);
            payment = paymentRepository.save(payment);
            
            // Log transaction
            logPaymentTransaction(payment.getId(), PaymentTransaction.TransactionType.PAYMENT, 
                PaymentStatus.PENDING, payment.getAmount(), "Payment URL created");
            
            log.info("VNPay payment URL created successfully for order: {} with txnRef: {}", 
                order.getId(), vnpTxnRef);
            
            return convertToResponseDTO(payment);
            
        } catch (Exception e) {
            log.error("Error creating VNPay payment URL for order: {}", request.getOrderId(), e);
            throw new RuntimeException("Failed to create payment URL: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentResponseDTO processReturnUrl(Map<String, String> vnpParams) {
        log.info("Processing VNPay return URL callback for txnRef: {}", vnpParams.get("vnp_TxnRef"));
        
        try {
            // Validate signature
            String secureHash = vnpParams.get("vnp_SecureHash");
            if (!VNPayUtil.validateSignature(vnpParams, secureHash, vnPayConfig.getHashSecret())) {
                log.error("Invalid VNPay signature for txnRef: {}", vnpParams.get("vnp_TxnRef"));
                throw new RuntimeException("Invalid payment signature");
            }
            
            String vnpTxnRef = vnpParams.get("vnp_TxnRef");
            Payment payment = paymentRepository.findByVnpTxnRef(vnpTxnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + vnpTxnRef));
            
            return updatePaymentStatus(payment, vnpParams);
            
        } catch (Exception e) {
            log.error("Error processing VNPay return URL", e);
            throw new RuntimeException("Failed to process return URL: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentResponseDTO processWebhook(VNPayCallbackRequestDTO callbackRequest) {
        log.info("Processing VNPay webhook for txnRef: {}", callbackRequest.getVnp_TxnRef());
        
        try {
            // Convert to map for signature validation
            Map<String, String> vnpParams = convertCallbackToMap(callbackRequest);
            
            // Validate signature
            if (!VNPayUtil.validateSignature(vnpParams, callbackRequest.getVnp_SecureHash(), vnPayConfig.getHashSecret())) {
                log.error("Invalid VNPay webhook signature for txnRef: {}", callbackRequest.getVnp_TxnRef());
                throw new RuntimeException("Invalid webhook signature");
            }
            
            Payment payment = paymentRepository.findByVnpTxnRef(callbackRequest.getVnp_TxnRef())
                .orElseThrow(() -> new RuntimeException("Payment not found: " + callbackRequest.getVnp_TxnRef()));
            
            return updatePaymentStatus(payment, vnpParams);
            
        } catch (Exception e) {
            log.error("Error processing VNPay webhook", e);
            throw new RuntimeException("Failed to process webhook: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponseDTO getPaymentByTxnRef(String vnpTxnRef) {
        Payment payment = paymentRepository.findByVnpTxnRef(vnpTxnRef)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + vnpTxnRef));
        return convertToResponseDTO(payment);
    }

    @Override
    public PaymentResponseDTO getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderIdAndPaymentStatus(orderId, PaymentStatus.PENDING)
            .or(() -> paymentRepository.findByOrderIdAndPaymentStatus(orderId, PaymentStatus.COMPLETED))
            .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        return convertToResponseDTO(payment);
    }

    @Override
    @Transactional
    public boolean cancelPayment(String paymentCode) {
        try {
            Payment payment = paymentRepository.findByPaymentCode(paymentCode)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentCode));
            
            if (!payment.isPending()) {
                throw new RuntimeException("Can only cancel pending payments");
            }
            
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setGatewayMessage("Cancelled by user");
            paymentRepository.save(payment);
            
            logPaymentTransaction(payment.getId(), PaymentTransaction.TransactionType.PAYMENT, 
                PaymentStatus.FAILED, payment.getAmount(), "Payment cancelled by user");
            
            log.info("Payment cancelled: {}", paymentCode);
            return true;
            
        } catch (Exception e) {
            log.error("Error cancelling payment: {}", paymentCode, e);
            return false;
        }
    }

    @Override
    @Transactional
    public PaymentResponseDTO refundPayment(String paymentCode, String reason) {
        log.info("Processing refund for payment: {}, reason: {}", paymentCode, reason);
        
        try {
            Payment payment = paymentRepository.findByPaymentCode(paymentCode)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentCode));
            
            if (!payment.isCompleted()) {
                throw new RuntimeException("Can only refund completed payments");
            }
            
            // In real implementation, call VNPay refund API here
            // For now, just update status
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            payment.setGatewayMessage("Refunded: " + reason);
            payment = paymentRepository.save(payment);
            
            // Update order status
            Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
            order.setStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);
            
            logPaymentTransaction(payment.getId(), PaymentTransaction.TransactionType.REFUND, 
                PaymentStatus.REFUNDED, payment.getAmount(), "Refund processed: " + reason);
            
            log.info("Payment refunded successfully: {}", paymentCode);
            return convertToResponseDTO(payment);
            
        } catch (Exception e) {
            log.error("Error processing refund for payment: {}", paymentCode, e);
            throw new RuntimeException("Failed to process refund: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponseDTO checkPaymentStatus(String vnpTxnRef) {
        // In real implementation, call VNPay query API
        // For now, return local payment status
        return getPaymentByTxnRef(vnpTxnRef);
    }

    @Override
    @Transactional
    public void processExpiredPayments() {
        log.info("Processing expired payments");
        
        try {
            var expiredPayments = paymentRepository.findExpiredPendingPayments(LocalDateTime.now());
            
            for (Payment payment : expiredPayments) {
                payment.setPaymentStatus(PaymentStatus.EXPIRED);
                payment.setGatewayMessage("Payment expired");
                paymentRepository.save(payment);
                
                logPaymentTransaction(payment.getId(), PaymentTransaction.TransactionType.TIMEOUT, 
                    PaymentStatus.EXPIRED, payment.getAmount(), "Payment expired");
                
                log.info("Payment expired: {} for order: {}", payment.getPaymentCode(), payment.getOrderId());
            }
            
            log.info("Processed {} expired payments", expiredPayments.size());
            
        } catch (Exception e) {
            log.error("Error processing expired payments", e);
        }
    }

    // Private helper methods
    
    private String buildVNPayUrl(Payment payment, PaymentCreateRequestDTO request) {
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(payment.getAmount().multiply(new BigDecimal("100")).longValue()));
        vnpParams.put("vnp_CurrCode", vnPayConfig.getCurrCode());
        vnpParams.put("vnp_TxnRef", payment.getVnpTxnRef());
        vnpParams.put("vnp_OrderInfo", payment.getVnpOrderInfo());
        vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnpParams.put("vnp_Locale", request.getLocale() != null ? request.getLocale() : vnPayConfig.getLocale());
        vnpParams.put("vnp_ReturnUrl", request.getReturnUrl() != null ? request.getReturnUrl() : vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", payment.getIpAddress());
        vnpParams.put("vnp_CreateDate", VNPayUtil.getPayDate());
        
        // Generate secure hash
        String secureHash = VNPayUtil.hashAllFields(vnpParams, vnPayConfig.getHashSecret());
        vnpParams.put("vnp_SecureHash", secureHash);
        
        // Build query string
        String query = VNPayUtil.buildQuery(vnpParams);
        return vnPayConfig.getUrl() + "?" + query;
    }
    
    private String generateTxnRef(Long orderId) {
        return "ORD" + orderId + "_" + System.currentTimeMillis();
    }
    
    private PaymentResponseDTO updatePaymentStatus(Payment payment, Map<String, String> vnpParams) {
        String responseCode = vnpParams.get("vnp_ResponseCode");
        String transactionStatus = vnpParams.get("vnp_TransactionStatus");
        String transactionNo = vnpParams.get("vnp_TransactionNo");
        
        PaymentStatus newStatus;
        String message;
        
        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            newStatus = PaymentStatus.COMPLETED;
            message = "Payment successful";
            payment.setPaidAt(LocalDateTime.now());
            
            // Update order status
            updateOrderStatus(payment.getOrderId(), OrderStatus.PAID);
        } else {
            newStatus = PaymentStatus.FAILED;
            message = "Payment failed - Response: " + responseCode + ", Status: " + transactionStatus;
        }
        
        payment.setPaymentStatus(newStatus);
        payment.setVnpTransactionNo(transactionNo);
        payment.setGatewayStatus(responseCode);
        payment.setGatewayMessage(message);
        
        payment = paymentRepository.save(payment);
        
        // Log transaction
        logPaymentTransaction(payment.getId(), PaymentTransaction.TransactionType.WEBHOOK, 
            newStatus, payment.getAmount(), message);
        
        log.info("Payment status updated: {} -> {}", payment.getVnpTxnRef(), newStatus);
        
        return convertToResponseDTO(payment);
    }
    
    private void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            order.setStatus(newStatus);
            orderRepository.save(order);
            log.info("Order status updated: {} -> {}", orderId, newStatus);
        } catch (Exception e) {
            log.error("Error updating order status for order: {}", orderId, e);
        }
    }
    
    private void logPaymentTransaction(Long paymentId, PaymentTransaction.TransactionType type, 
                                     PaymentStatus status, BigDecimal amount, String response) {
        try {
            PaymentTransaction transaction = PaymentTransaction.builder()
                .paymentId(paymentId)
                .transactionType(type)
                .status(status)
                .amount(amount)
                .gatewayResponse(response)
                .build();
            paymentTransactionRepository.save(transaction);
        } catch (Exception e) {
            log.error("Error logging payment transaction", e);
        }
    }
    
    private Map<String, String> convertCallbackToMap(VNPayCallbackRequestDTO callback) {
        Map<String, String> map = new HashMap<>();
        map.put("vnp_Amount", callback.getVnp_Amount());
        map.put("vnp_BankCode", callback.getVnp_BankCode());
        map.put("vnp_BankTranNo", callback.getVnp_BankTranNo());
        map.put("vnp_CardType", callback.getVnp_CardType());
        map.put("vnp_OrderInfo", callback.getVnp_OrderInfo());
        map.put("vnp_PayDate", callback.getVnp_PayDate());
        map.put("vnp_ResponseCode", callback.getVnp_ResponseCode());
        map.put("vnp_TmnCode", callback.getVnp_TmnCode());
        map.put("vnp_TransactionNo", callback.getVnp_TransactionNo());
        map.put("vnp_TransactionStatus", callback.getVnp_TransactionStatus());
        map.put("vnp_TxnRef", callback.getVnp_TxnRef());
        return map;
    }
    
    private PaymentResponseDTO convertToResponseDTO(Payment payment) {
        return PaymentResponseDTO.builder()
            .id(payment.getId())
            .paymentCode(payment.getPaymentCode())
            .orderId(payment.getOrderId())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .paymentMethod(payment.getPaymentMethod())
            .vnpTxnRef(payment.getVnpTxnRef())
            .vnpTransactionNo(payment.getVnpTransactionNo())
            .vnpOrderInfo(payment.getVnpOrderInfo())
            .vnpPaymentUrl(payment.getVnpPaymentUrl())
            .paymentStatus(payment.getPaymentStatus())
            .gatewayStatus(payment.getGatewayStatus())
            .gatewayMessage(payment.getGatewayMessage())
            .expiresAt(payment.getExpiresAt())
            .paidAt(payment.getPaidAt())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .expired(payment.isExpired())
            .pending(payment.isPending())
            .completed(payment.isCompleted())
            .failed(payment.isFailed())
            .build();
    }
}