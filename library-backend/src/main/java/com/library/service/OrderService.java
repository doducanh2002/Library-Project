package com.library.service;

import com.library.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    /**
     * Create order from user's cart
     */
    OrderDTO createOrderFromCart(Long userId, CreateOrderRequestDTO request);

    /**
     * Create order with payment integration
     */
    OrderWithPaymentDTO createOrderWithPayment(Long userId, CreateOrderRequestDTO request, String ipAddress, String userAgent);

    /**
     * Get user's order history
     */
    Page<OrderSummaryDTO> getUserOrderHistory(Long userId, Pageable pageable);

    /**
     * Get order details by order code
     */
    OrderDTO getOrderByCode(String orderCode, Long userId);

    /**
     * Cancel order (only if not yet paid)
     */
    OrderDTO cancelOrder(String orderCode, Long userId);

    /**
     * Get user's current orders (not delivered/cancelled)
     */
    List<OrderSummaryDTO> getUserCurrentOrders(Long userId);

    /**
     * Check if user can place new orders (business rules)
     */
    boolean canUserPlaceOrder(Long userId);

    /**
     * Calculate order totals before creating order
     */
    OrderCalculationDTO calculateOrderTotals(Long userId);

    /**
     * Get detailed order history with enhanced information
     */
    Page<OrderHistoryDTO> getDetailedOrderHistory(Long userId, Pageable pageable);

    /**
     * Get order statistics for user
     */
    UserOrderStatsDTO getUserOrderStatistics(Long userId);

    /**
     * Reorder - create new order from existing order
     */
    OrderCalculationDTO reorderFromExistingOrder(String orderCode, Long userId);

    /**
     * Track order status
     */
    OrderTrackingDTO trackOrder(String orderCode, Long userId);
}