package com.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "quantity", nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Column(name = "price_per_unit", nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Price per unit must be non-negative")
    private BigDecimal pricePerUnit;

    @Column(name = "item_total_price", nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Item total price must be non-negative")
    private BigDecimal itemTotalPrice;

    // Snapshot data (at time of order)
    @Column(name = "book_title", nullable = false)
    private String bookTitle;

    @Column(name = "book_isbn", nullable = false)
    private String bookIsbn;

    // Helper method to calculate total price
    public void calculateItemTotalPrice() {
        if (this.quantity != null && this.pricePerUnit != null) {
            this.itemTotalPrice = this.pricePerUnit.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    @PrePersist
    @PreUpdate
    private void validateAndCalculate() {
        calculateItemTotalPrice();
    }

    // Constructor for creating from cart item
    public static OrderItem fromCartItem(CartItem cartItem, Order order) {
        return OrderItem.builder()
                .order(order)
                .book(cartItem.getBook())
                .quantity(cartItem.getQuantity())
                .pricePerUnit(cartItem.getBook().getPrice())
                .bookTitle(cartItem.getBook().getTitle())
                .bookIsbn(cartItem.getBook().getIsbn())
                .build();
    }
}