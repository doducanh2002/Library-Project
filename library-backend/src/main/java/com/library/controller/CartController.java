package com.library.controller;

import com.library.dto.*;
import com.library.service.CartService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shopping Cart", description = "Shopping cart management APIs")
@CrossOrigin(origins = "*")
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get cart items", description = "Get all items in the user's cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart items retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<CartItemDTO>> getCartItems(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Getting cart items for user: {}", userId);
        List<CartItemDTO> items = cartService.getCartItems(userId);
        return BaseResponse.success(items);
    }
    
    @GetMapping("/summary")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get cart summary", description = "Get cart summary with totals and validation status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart summary retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<CartSummaryDTO> getCartSummary(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Getting cart summary for user: {}", userId);
        CartSummaryDTO summary = cartService.getCartSummary(userId);
        return BaseResponse.success(summary);
    }
    
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Add item to cart", description = "Add a book to the user's shopping cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Item added to cart successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "409", description = "Insufficient stock")
    })
    public BaseResponse<CartItemDTO> addToCart(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddToCartRequestDTO request) {
        
        log.info("Adding item to cart for user: {}, request: {}", userId, request);
        CartItemDTO item = cartService.addToCart(userId, request);
        return BaseResponse.success(item);
    }
    
    @PutMapping("/items/{bookId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Update cart item", description = "Update quantity of an item in the cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart item updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Cart item not found"),
        @ApiResponse(responseCode = "409", description = "Insufficient stock")
    })
    public BaseResponse<CartItemDTO> updateCartItem(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Book ID", required = true) @PathVariable Long bookId,
            @Valid @RequestBody UpdateCartItemRequestDTO request) {
        
        log.info("Updating cart item for user: {}, bookId: {}, request: {}", userId, bookId, request);
        CartItemDTO item = cartService.updateCartItem(userId, bookId, request);
        return BaseResponse.success(item);
    }
    
    @DeleteMapping("/items/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Remove item from cart", description = "Remove a specific item from the cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Item removed from cart successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    public BaseResponse<Void> removeFromCart(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Book ID", required = true) @PathVariable Long bookId) {
        
        log.info("Removing item from cart for user: {}, bookId: {}", userId, bookId);
        cartService.removeFromCart(userId, bookId);
        return BaseResponse.success(null);
    }
    
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Clear cart", description = "Remove all items from the cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cart cleared successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Void> clearCart(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Clearing cart for user: {}", userId);
        cartService.clearCart(userId);
        return BaseResponse.success(null);
    }
    
    @GetMapping("/count")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get cart item count", description = "Get the number of items in the cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart count retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Integer> getCartItemCount(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Getting cart item count for user: {}", userId);
        int count = cartService.getCartItemCount(userId);
        return BaseResponse.success(count);
    }
    
    @GetMapping("/quantity")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get total quantity", description = "Get the total quantity of all items in the cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total quantity retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Integer> getTotalQuantity(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Getting total quantity for user: {}", userId);
        int quantity = cartService.getTotalQuantity(userId);
        return BaseResponse.success(quantity);
    }
    
    @PostMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Validate cart", description = "Validate cart for checkout and sync with inventory")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart validation completed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Boolean> validateCart(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Validating cart for user: {}", userId);
        cartService.syncCartWithInventory(userId);
        boolean isValid = cartService.validateCartForCheckout(userId);
        return BaseResponse.success(isValid);
    }
}