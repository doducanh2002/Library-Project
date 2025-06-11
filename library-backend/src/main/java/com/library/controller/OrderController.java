package com.library.controller;

import com.library.dto.*;
import com.library.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Create order from cart", description = "Create a new order from user's shopping cart")
    public ResponseEntity<BaseResponse<OrderDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating order for user: {}", userDetails.getUsername());
        
        // Extract user ID from UserDetails (assuming username is the ID for now)
        Long userId = Long.parseLong(userDetails.getUsername());
        
        if (!orderService.canUserPlaceOrder(userId)) {
            return ResponseEntity.badRequest().body(
                BaseResponse.<OrderDTO>builder()
                    .success(false)
                    .message("Cannot place order. You have too many pending orders.")
                    .build()
            );
        }

        OrderDTO order = orderService.createOrderFromCart(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            BaseResponse.<OrderDTO>builder()
                .success(true)
                .message("Order created successfully")
                .data(order)
                .build()
        );
    }

    @GetMapping("/calculate")
    @Operation(summary = "Calculate order totals", description = "Calculate order totals before checkout")
    public ResponseEntity<BaseResponse<OrderCalculationDTO>> calculateOrderTotals(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        OrderCalculationDTO calculation = orderService.calculateOrderTotals(userId);
        
        return ResponseEntity.ok(
            BaseResponse.<OrderCalculationDTO>builder()
                .success(true)
                .message("Order calculation completed")
                .data(calculation)
                .build()
        );
    }

    @GetMapping
    @Operation(summary = "Get user order history", description = "Get paginated list of user's orders")
    public ResponseEntity<BaseResponse<Page<OrderSummaryDTO>>> getUserOrderHistory(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderSummaryDTO> orders = orderService.getUserOrderHistory(userId, pageable);
        
        return ResponseEntity.ok(
            BaseResponse.<Page<OrderSummaryDTO>>builder()
                .success(true)
                .message("Order history retrieved successfully")
                .data(orders)
                .build()
        );
    }

    @GetMapping("/current")
    @Operation(summary = "Get current orders", description = "Get user's current active orders")
    public ResponseEntity<BaseResponse<List<OrderSummaryDTO>>> getCurrentOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        List<OrderSummaryDTO> orders = orderService.getUserCurrentOrders(userId);
        
        return ResponseEntity.ok(
            BaseResponse.<List<OrderSummaryDTO>>builder()
                .success(true)
                .message("Current orders retrieved successfully")
                .data(orders)
                .build()
        );
    }

    @GetMapping("/{orderCode}")
    @Operation(summary = "Get order details", description = "Get detailed information about a specific order")
    public ResponseEntity<BaseResponse<OrderDTO>> getOrderDetails(
            @Parameter(description = "Order code") @PathVariable String orderCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        OrderDTO order = orderService.getOrderByCode(orderCode, userId);
        
        return ResponseEntity.ok(
            BaseResponse.<OrderDTO>builder()
                .success(true)
                .message("Order details retrieved successfully")
                .data(order)
                .build()
        );
    }

    @PostMapping("/{orderCode}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order (only if not yet paid)")
    public ResponseEntity<BaseResponse<OrderDTO>> cancelOrder(
            @Parameter(description = "Order code") @PathVariable String orderCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        OrderDTO order = orderService.cancelOrder(orderCode, userId);
        
        return ResponseEntity.ok(
            BaseResponse.<OrderDTO>builder()
                .success(true)
                .message("Order cancelled successfully")
                .data(order)
                .build()
        );
    }

    @GetMapping("/can-place-order")
    @Operation(summary = "Check if user can place order", description = "Check if user meets requirements to place a new order")
    public ResponseEntity<BaseResponse<Boolean>> canPlaceOrder(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        boolean canPlace = orderService.canUserPlaceOrder(userId);
        
        return ResponseEntity.ok(
            BaseResponse.<Boolean>builder()
                .success(true)
                .message(canPlace ? "User can place order" : "User cannot place order")
                .data(canPlace)
                .build()
        );
    }

    @GetMapping("/history")
    @Operation(summary = "Get detailed order history", description = "Get detailed order history with enhanced information")
    public ResponseEntity<BaseResponse<Page<OrderHistoryDTO>>> getDetailedOrderHistory(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderHistoryDTO> history = orderService.getDetailedOrderHistory(userId, pageable);
        
        return ResponseEntity.ok(
            BaseResponse.<Page<OrderHistoryDTO>>builder()
                .success(true)
                .message("Detailed order history retrieved successfully")
                .data(history)
                .build()
        );
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get user order statistics", description = "Get comprehensive statistics about user's ordering behavior")
    public ResponseEntity<BaseResponse<UserOrderStatsDTO>> getUserOrderStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        UserOrderStatsDTO stats = orderService.getUserOrderStatistics(userId);
        
        return ResponseEntity.ok(
            BaseResponse.<UserOrderStatsDTO>builder()
                .success(true)
                .message("Order statistics retrieved successfully")
                .data(stats)
                .build()
        );
    }

    @PostMapping("/{orderCode}/reorder")
    @Operation(summary = "Reorder from existing order", description = "Create new cart from existing order for reordering")
    public ResponseEntity<BaseResponse<OrderCalculationDTO>> reorderFromExistingOrder(
            @Parameter(description = "Order code") @PathVariable String orderCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        OrderCalculationDTO calculation = orderService.reorderFromExistingOrder(orderCode, userId);
        
        return ResponseEntity.ok(
            BaseResponse.<OrderCalculationDTO>builder()
                .success(true)
                .message("Items added to cart from previous order")
                .data(calculation)
                .build()
        );
    }

    @GetMapping("/{orderCode}/track")
    @Operation(summary = "Track order", description = "Get detailed tracking information for an order")
    public ResponseEntity<BaseResponse<OrderTrackingDTO>> trackOrder(
            @Parameter(description = "Order code") @PathVariable String orderCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        OrderTrackingDTO tracking = orderService.trackOrder(orderCode, userId);
        
        return ResponseEntity.ok(
            BaseResponse.<OrderTrackingDTO>builder()
                .success(true)
                .message("Order tracking information retrieved successfully")
                .data(tracking)
                .build()
        );
    }
}