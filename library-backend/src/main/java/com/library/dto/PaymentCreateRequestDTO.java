package com.library.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateRequestDTO {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment method is required")
    @Pattern(regexp = "^(VNPAY_QR|VNPAY_CARD|VNPAY_ATM|VNPAY_BANK)$", 
             message = "Payment method must be one of: VNPAY_QR, VNPAY_CARD, VNPAY_ATM, VNPAY_BANK")
    private String paymentMethod;

    private String returnUrl;
    
    private String ipAddress;
    
    private String userAgent;
    
    private String locale = "vn";
}