package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestDTO {

    // Shipping information
    @NotBlank(message = "Shipping address line 1 is required")
    @Size(max = 255, message = "Shipping address line 1 must not exceed 255 characters")
    private String shippingAddressLine1;

    @Size(max = 255, message = "Shipping address line 2 must not exceed 255 characters")
    private String shippingAddressLine2;

    @NotBlank(message = "Shipping city is required")
    @Size(max = 100, message = "Shipping city must not exceed 100 characters")
    private String shippingCity;

    @Size(max = 20, message = "Shipping postal code must not exceed 20 characters")
    private String shippingPostalCode;

    @Size(max = 50, message = "Shipping country must not exceed 50 characters")
    @Builder.Default
    private String shippingCountry = "Vietnam";

    // Customer note
    @Size(max = 1000, message = "Customer note must not exceed 1000 characters")
    private String customerNote;

    // Payment method preference
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;
}