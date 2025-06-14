package com.library.controller;

import com.library.dto.BaseResponse;
import com.library.dto.PaymentResponseDTO;
import com.library.dto.ProcessRefundRequestDTO;
import com.library.service.VNPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Payment Management", description = "Admin APIs for payment management")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
public class AdminPaymentController {

    private final VNPayService vnPayService;

    @PostMapping("/{paymentCode}/refund")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Process refund", description = "Process refund for a completed payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid refund request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public BaseResponse<PaymentResponseDTO> processRefund(
            @Parameter(description = "Payment code") 
            @PathVariable String paymentCode,
            @Valid @RequestBody ProcessRefundRequestDTO request) {
        
        log.info("Processing refund for payment: {}, reason: {}", paymentCode, request.getReason());
        
        PaymentResponseDTO response = vnPayService.refundPayment(paymentCode, request.getReason());
        return BaseResponse.success(response, "Refund processed successfully");
    }

    @PostMapping("/process-expired")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Process expired payments", description = "Manually trigger expired payment processing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Expired payments processed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<String> processExpiredPayments() {
        log.info("Manually triggering expired payment processing");
        
        vnPayService.processExpiredPayments();
        return BaseResponse.success("Processing completed", "Expired payments processed successfully");
    }
}