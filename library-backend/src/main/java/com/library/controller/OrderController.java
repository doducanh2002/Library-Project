package com.library.controller;

import com.library.dto.*;
import com.library.service.OrderService;
import com.library.util.VNPayUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Create order from cart", description = "Create a new order from user's shopping cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or cannot place order"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "409", description = "Cart is empty or insufficient stock")
    })
    public BaseResponse<OrderDTO> createOrder(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateOrderRequestDTO request) {
        
        log.info("Creating order for user: {}, request: {}", userId, request);
        
        if (!orderService.canUserPlaceOrder(userId)) {
            throw new IllegalStateException("Cannot place order. You have too many pending orders.");
        }

        OrderDTO order = orderService.createOrderFromCart(userId, request);
        return BaseResponse.success(order);
    }

    @PostMapping("/checkout-with-payment")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Complete checkout with payment", description = "Create order and initiate payment process")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created and payment initiated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or cannot place order"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "409", description = "Cart is empty or insufficient stock")
    })
    public BaseResponse<OrderWithPaymentDTO> createOrderWithPayment(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateOrderRequestDTO request,
            HttpServletRequest httpRequest) {
        
        log.info("Creating order with payment for user: {}, request: {}", userId, request);
        
        if (!orderService.canUserPlaceOrder(userId)) {
            throw new IllegalStateException("Cannot place order. You have too many pending orders.");
        }

        // Get client IP and User Agent
        String ipAddress = VNPayUtil.getIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        OrderWithPaymentDTO orderWithPayment = orderService.createOrderWithPayment(userId, request, ipAddress, userAgent);
        return BaseResponse.success(orderWithPayment);
    }

    @GetMapping("/calculate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Calculate order totals", description = "Calculate order totals before checkout")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order calculation completed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<OrderCalculationDTO> calculateOrderTotals(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Calculating order totals for user: {}", userId);
        OrderCalculationDTO calculation = orderService.calculateOrderTotals(userId);
        return BaseResponse.success(calculation);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get user order history", description = "Get paginated list of user's orders")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order history retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Page<OrderSummaryDTO>> getUserOrderHistory(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        log.info("Getting order history for user: {}, page: {}, size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderSummaryDTO> orders = orderService.getUserOrderHistory(userId, pageable);
        return BaseResponse.success(orders);
    }

    @GetMapping("/current")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get current orders", description = "Get user's current active orders")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<OrderSummaryDTO>> getCurrentOrders(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Getting current orders for user: {}", userId);
        List<OrderSummaryDTO> orders = orderService.getUserCurrentOrders(userId);
        return BaseResponse.success(orders);
    }

    @GetMapping("/{orderCode}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get order details", description = "Get detailed information about a specific order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order details retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public BaseResponse<OrderDTO> getOrderDetails(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Order code") @PathVariable String orderCode) {
        
        log.info("Getting order details for user: {}, orderCode: {}", userId, orderCode);
        OrderDTO order = orderService.getOrderByCode(orderCode, userId);
        return BaseResponse.success(order);
    }

    @PostMapping("/{orderCode}/cancel")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Cancel order", description = "Cancel an order (only if not yet paid)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel order"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public BaseResponse<OrderDTO> cancelOrder(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Order code") @PathVariable String orderCode) {
        
        log.info("Cancelling order for user: {}, orderCode: {}", userId, orderCode);
        OrderDTO order = orderService.cancelOrder(orderCode, userId);
        return BaseResponse.success(order);
    }

    @GetMapping("/can-place-order")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Check if user can place order", description = "Check if user meets requirements to place a new order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check completed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Boolean> canPlaceOrder(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Checking if user can place order: {}", userId);
        boolean canPlace = orderService.canUserPlaceOrder(userId);
        return BaseResponse.success(canPlace);
    }

    @GetMapping("/history")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get detailed order history", description = "Get detailed order history with enhanced information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detailed order history retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Page<OrderHistoryDTO>> getDetailedOrderHistory(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        log.info("Getting detailed order history for user: {}, page: {}, size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderHistoryDTO> history = orderService.getDetailedOrderHistory(userId, pageable);
        return BaseResponse.success(history);
    }

    @GetMapping("/statistics")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get user order statistics", description = "Get comprehensive statistics about user's ordering behavior")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<UserOrderStatsDTO> getUserOrderStatistics(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Getting order statistics for user: {}", userId);
        UserOrderStatsDTO stats = orderService.getUserOrderStatistics(userId);
        return BaseResponse.success(stats);
    }

    @PostMapping("/{orderCode}/reorder")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Reorder from existing order", description = "Create new cart from existing order for reordering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Items added to cart successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public BaseResponse<OrderCalculationDTO> reorderFromExistingOrder(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Order code") @PathVariable String orderCode) {
        
        log.info("Reordering from existing order for user: {}, orderCode: {}", userId, orderCode);
        OrderCalculationDTO calculation = orderService.reorderFromExistingOrder(orderCode, userId);
        return BaseResponse.success(calculation);
    }

    @GetMapping("/{orderCode}/track")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Track order", description = "Get detailed tracking information for an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order tracking information retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public BaseResponse<OrderTrackingDTO> trackOrder(
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Order code") @PathVariable String orderCode) {
        
        log.info("Tracking order for user: {}, orderCode: {}", userId, orderCode);
        OrderTrackingDTO tracking = orderService.trackOrder(orderCode, userId);
        return BaseResponse.success(tracking);
    }
}