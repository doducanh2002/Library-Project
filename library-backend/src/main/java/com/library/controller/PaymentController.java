package com.library.controller;

import com.library.dto.BaseResponse;
import com.library.dto.PaymentCreateRequestDTO;
import com.library.dto.PaymentResponseDTO;
import com.library.dto.VNPayCallbackRequestDTO;
import com.library.service.VNPayService;
import com.library.util.VNPayUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for VNPay payment processing")
public class PaymentController {

    private final VNPayService vnPayService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create VNPay payment URL", description = "Generate VNPay payment URL for an order")
    public ResponseEntity<BaseResponse<PaymentResponseDTO>> createPayment(
            @Valid @RequestBody PaymentCreateRequestDTO request,
            HttpServletRequest httpRequest) {
        
        log.info("Creating VNPay payment for order: {}", request.getOrderId());
        
        try {
            // Set IP address if not provided
            if (request.getIpAddress() == null) {
                request.setIpAddress(VNPayUtil.getIpAddress(httpRequest));
            }
            
            // Set user agent if not provided
            if (request.getUserAgent() == null) {
                request.setUserAgent(httpRequest.getHeader("User-Agent"));
            }
            
            PaymentResponseDTO response = vnPayService.createPaymentUrl(request, httpRequest);
            
            return ResponseEntity.ok(BaseResponse.<PaymentResponseDTO>builder()
                .success(true)
                .message("Payment URL created successfully")
                .data(response)
                .build());
                
        } catch (Exception e) {
            log.error("Error creating payment for order: {}", request.getOrderId(), e);
            return ResponseEntity.badRequest().body(BaseResponse.<PaymentResponseDTO>builder()
                .success(false)
                .message("Failed to create payment: " + e.getMessage())
                .build());
        }
    }

    @GetMapping("/return")
    @Operation(summary = "VNPay return URL", description = "Handle VNPay return URL callback")
    public ResponseEntity<BaseResponse<PaymentResponseDTO>> handleReturnUrl(HttpServletRequest request) {
        log.info("Processing VNPay return URL callback");
        
        try {
            // Convert request parameters to map
            Map<String, String> vnpParams = new HashMap<>();
            request.getParameterMap().forEach((key, value) -> {
                if (value.length > 0) {
                    vnpParams.put(key, value[0]);
                }
            });
            
            PaymentResponseDTO response = vnPayService.processReturnUrl(vnpParams);
            
            return ResponseEntity.ok(BaseResponse.<PaymentResponseDTO>builder()
                .success(true)
                .message("Payment processed successfully")
                .data(response)
                .build());
                
        } catch (Exception e) {
            log.error("Error processing VNPay return URL", e);
            return ResponseEntity.badRequest().body(BaseResponse.<PaymentResponseDTO>builder()
                .success(false)
                .message("Failed to process payment return: " + e.getMessage())
                .build());
        }
    }

    @PostMapping("/webhook/vnpay")
    @Operation(summary = "VNPay webhook (IPN)", description = "Handle VNPay webhook notifications")
    public ResponseEntity<String> handleWebhook(@RequestBody VNPayCallbackRequestDTO callbackRequest) {
        log.info("Processing VNPay webhook for txnRef: {}", callbackRequest.getVnp_TxnRef());
        
        try {
            PaymentResponseDTO response = vnPayService.processWebhook(callbackRequest);
            
            // VNPay expects specific response format
            if (response.isCompleted()) {
                return ResponseEntity.ok("{\"RspCode\":\"00\",\"Message\":\"success\"}");
            } else {
                return ResponseEntity.ok("{\"RspCode\":\"01\",\"Message\":\"failed\"}");
            }
            
        } catch (Exception e) {
            log.error("Error processing VNPay webhook", e);
            return ResponseEntity.ok("{\"RspCode\":\"99\",\"Message\":\"error\"}");
        }
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get payment details", description = "Retrieve payment information by ID")
    public ResponseEntity<BaseResponse<PaymentResponseDTO>> getPayment(
            @Parameter(description = "Payment ID or transaction reference") 
            @PathVariable String paymentId) {
        
        try {
            PaymentResponseDTO response;
            
            // Try to find by transaction reference first, then by payment code
            try {
                response = vnPayService.getPaymentByTxnRef(paymentId);
            } catch (Exception e) {
                // If not found by txnRef, it might be a payment code
                log.debug("Payment not found by txnRef, trying other methods");
                throw new RuntimeException("Payment not found: " + paymentId);
            }
            
            return ResponseEntity.ok(BaseResponse.<PaymentResponseDTO>builder()
                .success(true)
                .message("Payment retrieved successfully")
                .data(response)
                .build());
                
        } catch (Exception e) {
            log.error("Error retrieving payment: {}", paymentId, e);
            return ResponseEntity.badRequest().body(BaseResponse.<PaymentResponseDTO>builder()
                .success(false)
                .message("Payment not found: " + e.getMessage())
                .build());
        }
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get payment by order", description = "Retrieve payment information for an order")
    public ResponseEntity<BaseResponse<PaymentResponseDTO>> getPaymentByOrder(
            @Parameter(description = "Order ID") 
            @PathVariable Long orderId) {
        
        try {
            PaymentResponseDTO response = vnPayService.getPaymentByOrderId(orderId);
            
            return ResponseEntity.ok(BaseResponse.<PaymentResponseDTO>builder()
                .success(true)
                .message("Payment retrieved successfully")
                .data(response)
                .build());
                
        } catch (Exception e) {
            log.error("Error retrieving payment for order: {}", orderId, e);
            return ResponseEntity.badRequest().body(BaseResponse.<PaymentResponseDTO>builder()
                .success(false)
                .message("Payment not found for order: " + e.getMessage())
                .build());
        }
    }

    @PostMapping("/{paymentCode}/cancel")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Cancel payment", description = "Cancel a pending payment")
    public ResponseEntity<BaseResponse<Boolean>> cancelPayment(
            @Parameter(description = "Payment code") 
            @PathVariable String paymentCode) {
        
        try {
            boolean cancelled = vnPayService.cancelPayment(paymentCode);
            
            return ResponseEntity.ok(BaseResponse.<Boolean>builder()
                .success(true)
                .message(cancelled ? "Payment cancelled successfully" : "Failed to cancel payment")
                .data(cancelled)
                .build());
                
        } catch (Exception e) {
            log.error("Error cancelling payment: {}", paymentCode, e);
            return ResponseEntity.badRequest().body(BaseResponse.<Boolean>builder()
                .success(false)
                .message("Failed to cancel payment: " + e.getMessage())
                .build());
        }
    }

    @GetMapping("/status/{vnpTxnRef}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Check payment status", description = "Check payment status from VNPay")
    public ResponseEntity<BaseResponse<PaymentResponseDTO>> checkPaymentStatus(
            @Parameter(description = "VNPay transaction reference") 
            @PathVariable String vnpTxnRef) {
        
        try {
            PaymentResponseDTO response = vnPayService.checkPaymentStatus(vnpTxnRef);
            
            return ResponseEntity.ok(BaseResponse.<PaymentResponseDTO>builder()
                .success(true)
                .message("Payment status retrieved successfully")
                .data(response)
                .build());
                
        } catch (Exception e) {
            log.error("Error checking payment status: {}", vnpTxnRef, e);
            return ResponseEntity.badRequest().body(BaseResponse.<PaymentResponseDTO>builder()
                .success(false)
                .message("Failed to check payment status: " + e.getMessage())
                .build());
        }
    }
}