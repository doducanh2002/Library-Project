package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryDTO {
    private Long userId;
    private List<CartItemDTO> items;
    private Integer totalItems;
    private Integer totalQuantity;
    private BigDecimal totalPrice;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shipping;
    private Boolean isValid;
    private List<String> validationErrors;
    private Boolean canCheckout;
}