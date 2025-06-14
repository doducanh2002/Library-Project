package com.library.controller;

import com.library.dto.BaseResponse;
import com.library.dto.PaymentResponseDTO;
import com.library.dto.ProcessRefundRequestDTO;
import com.library.service.VNPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Payment Management", description = "Admin APIs for payment management")
@PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
public class AdminPaymentController {

    private final VNPayService vnPayService;

    @PostMapping("/{paymentCode}/refund")
    @Operation(summary = "Process refund", description = "Process refund for a completed payment")
    public ResponseEntity<BaseResponse<PaymentResponseDTO>> processRefund(
            @Parameter(description = "Payment code") 
            @PathVariable String paymentCode,
            @Valid @RequestBody ProcessRefundRequestDTO request) {
        
        log.info("Processing refund for payment: {}, reason: {}", paymentCode, request.getReason());
        
        try {
            PaymentResponseDTO response = vnPayService.refundPayment(paymentCode, request.getReason());
            
            return ResponseEntity.ok(BaseResponse.<PaymentResponseDTO>builder()
                .success(true)
                .message("Refund processed successfully")
                .data(response)
                .build());
                
        } catch (Exception e) {
            log.error("Error processing refund for payment: {}", paymentCode, e);
            return ResponseEntity.badRequest().body(BaseResponse.<PaymentResponseDTO>builder()
                .success(false)
                .message("Failed to process refund: " + e.getMessage())
                .build());
        }
    }

    @PostMapping("/process-expired")
    @Operation(summary = "Process expired payments", description = "Manually trigger expired payment processing")
    public ResponseEntity<BaseResponse<String>> processExpiredPayments() {
        log.info("Manually triggering expired payment processing");
        
        try {
            vnPayService.processExpiredPayments();
            
            return ResponseEntity.ok(BaseResponse.<String>builder()
                .success(true)
                .message("Expired payments processed successfully")
                .data("Processing completed")
                .build());
                
        } catch (Exception e) {
            log.error("Error processing expired payments", e);
            return ResponseEntity.badRequest().body(BaseResponse.<String>builder()
                .success(false)
                .message("Failed to process expired payments: " + e.getMessage())
                .build());
        }
    }
}