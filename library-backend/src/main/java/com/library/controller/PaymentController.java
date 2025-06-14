package com.library.controller;

import com.library.dto.BaseResponse;
import com.library.dto.PaymentCreateRequestDTO;
import com.library.dto.PaymentResponseDTO;
import com.library.dto.VNPayCallbackRequestDTO;
import com.library.service.VNPayService;
import com.library.util.VNPayUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for VNPay payment processing")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final VNPayService vnPayService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Create VNPay payment URL", description = "Generate VNPay payment URL for an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment URL created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<PaymentResponseDTO> createPayment(
            @Valid @RequestBody PaymentCreateRequestDTO request,
            HttpServletRequest httpRequest) {
        
        log.info("Creating VNPay payment for order: {}", request.getOrderId());
        
        // Set IP address if not provided
        if (request.getIpAddress() == null) {
            request.setIpAddress(VNPayUtil.getIpAddress(httpRequest));
        }
        
        // Set user agent if not provided
        if (request.getUserAgent() == null) {
            request.setUserAgent(httpRequest.getHeader("User-Agent"));
        }
        
        PaymentResponseDTO response = vnPayService.createPaymentUrl(request, httpRequest);
        return BaseResponse.success(response, "Payment URL created successfully");
    }

    @GetMapping("/return")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "VNPay return URL", description = "Handle VNPay return URL callback")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment return processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid return parameters")
    })
    public BaseResponse<PaymentResponseDTO> handleReturnUrl(HttpServletRequest request) {
        log.info("Processing VNPay return URL callback");
        
        // Convert request parameters to map
        Map<String, String> vnpParams = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> {
            if (value.length > 0) {
                vnpParams.put(key, value[0]);
            }
        });
        
        PaymentResponseDTO response = vnPayService.processReturnUrl(vnpParams);
        return BaseResponse.success(response, "Payment processed successfully");
    }

    @PostMapping("/webhook/vnpay")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "VNPay webhook (IPN)", description = "Handle VNPay webhook notifications")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook processed")
    })
    public String handleWebhook(@RequestBody VNPayCallbackRequestDTO callbackRequest) {
        log.info("Processing VNPay webhook for txnRef: {}", callbackRequest.getVnp_TxnRef());
        
        try {
            PaymentResponseDTO response = vnPayService.processWebhook(callbackRequest);
            
            // VNPay expects specific response format
            if (response.isCompleted()) {
                return "{\"RspCode\":\"00\",\"Message\":\"success\"}";
            } else {
                return "{\"RspCode\":\"01\",\"Message\":\"failed\"}";
            }
            
        } catch (Exception e) {
            log.error("Error processing VNPay webhook", e);
            return "{\"RspCode\":\"99\",\"Message\":\"error\"}";
        }
    }

    @GetMapping("/{paymentId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get payment details", description = "Retrieve payment information by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public BaseResponse<PaymentResponseDTO> getPayment(
            @Parameter(description = "Payment ID or transaction reference") 
            @PathVariable String paymentId) {
        
        log.info("Getting payment details for ID: {}", paymentId);
        
        PaymentResponseDTO response;
        
        // Try to find by transaction reference first, then by payment code
        try {
            response = vnPayService.getPaymentByTxnRef(paymentId);
        } catch (Exception e) {
            // If not found by txnRef, it might be a payment code
            log.debug("Payment not found by txnRef, trying other methods");
            throw new RuntimeException("Payment not found: " + paymentId);
        }
        
        return BaseResponse.success(response, "Payment retrieved successfully");
    }

    @GetMapping("/order/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get payment by order", description = "Retrieve payment information for an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Payment not found for order")
    })
    public BaseResponse<PaymentResponseDTO> getPaymentByOrder(
            @Parameter(description = "Order ID") 
            @PathVariable Long orderId) {
        
        log.info("Getting payment details for order: {}", orderId);
        
        PaymentResponseDTO response = vnPayService.getPaymentByOrderId(orderId);
        return BaseResponse.success(response, "Payment retrieved successfully");
    }

    @PostMapping("/{paymentCode}/cancel")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Cancel payment", description = "Cancel a pending payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment cancellation processed"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel payment"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public BaseResponse<Boolean> cancelPayment(
            @Parameter(description = "Payment code") 
            @PathVariable String paymentCode) {
        
        log.info("Cancelling payment: {}", paymentCode);
        
        boolean cancelled = vnPayService.cancelPayment(paymentCode);
        String message = cancelled ? "Payment cancelled successfully" : "Failed to cancel payment";
        return BaseResponse.success(cancelled, message);
    }

    @GetMapping("/status/{vnpTxnRef}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Check payment status", description = "Check payment status from VNPay")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment status retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public BaseResponse<PaymentResponseDTO> checkPaymentStatus(
            @Parameter(description = "VNPay transaction reference") 
            @PathVariable String vnpTxnRef) {
        
        log.info("Checking payment status for txnRef: {}", vnpTxnRef);
        
        PaymentResponseDTO response = vnPayService.checkPaymentStatus(vnpTxnRef);
        return BaseResponse.success(response, "Payment status retrieved successfully");
    }
}