package com.library.service;

import com.library.dto.CartItemDTO;
import com.library.dto.CartSummaryDTO;
import com.library.dto.AddToCartRequestDTO;
import com.library.dto.UpdateCartItemRequestDTO;

import java.util.List;

public interface CartService {
    
    // Cart Item Operations
    CartItemDTO addToCart(Long userId, AddToCartRequestDTO request);
    
    CartItemDTO updateCartItem(Long userId, Long bookId, UpdateCartItemRequestDTO request);
    
    void removeFromCart(Long userId, Long bookId);
    
    void clearCart(Long userId);
    
    // Cart Retrieval
    List<CartItemDTO> getCartItems(Long userId);
    
    CartSummaryDTO getCartSummary(Long userId);
    
    // Cart Validation
    List<CartItemDTO> getValidCartItems(Long userId);
    
    List<CartItemDTO> getInvalidCartItems(Long userId);
    
    boolean validateCartForCheckout(Long userId);
    
    // Cart Statistics
    int getCartItemCount(Long userId);
    
    int getTotalQuantity(Long userId);
    
    // Business Logic
    boolean canAddToCart(Long userId, Long bookId, int quantity);
    
    void validateCartItem(Long userId, Long bookId, int quantity);
    
    void syncCartWithInventory(Long userId);
}