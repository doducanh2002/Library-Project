package com.library.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessRefundRequestDTO {

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal refundAmount;

    @NotNull(message = "Refund reason is required")
    @Size(min = 5, max = 500, message = "Refund reason must be between 5 and 500 characters")
    private String reason;

    @Size(max = 100, message = "Refund transaction ID must not exceed 100 characters")
    private String refundTransactionId;

    private boolean restoreStock;
}