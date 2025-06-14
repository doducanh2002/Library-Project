package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderWithPaymentDTO {
    
    private OrderDTO order;
    private PaymentResponseDTO payment;
    private String nextAction; // "redirect_to_payment" hoặc "payment_completed"
    private String message;
}