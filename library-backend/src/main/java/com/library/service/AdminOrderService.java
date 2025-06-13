package com.library.service;

import com.library.dto.*;
import com.library.entity.OrderStatus;
import com.library.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminOrderService {

    /**
     * Get all orders with admin view
     */
    Page<AdminOrderDTO> getAllOrders(Pageable pageable);

    /**
     * Get orders by status
     */
    Page<AdminOrderDTO> getOrdersByStatus(OrderStatus status, Pageable pageable);

    /**
     * Get orders by payment status
     */
    Page<AdminOrderDTO> getOrdersByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    /**
     * Get order details for admin
     */
    AdminOrderDetailDTO getOrderDetails(Long orderId);

    /**
     * Update order status
     */
    AdminOrderDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequestDTO request);

    /**
     * Update payment status
     */
    AdminOrderDTO updatePaymentStatus(Long orderId, UpdatePaymentStatusRequestDTO request);

    /**
     * Process refund
     */
    AdminOrderDTO processRefund(Long orderId, ProcessRefundRequestDTO request);

    /**
     * Get orders needing attention
     */
    List<AdminOrderDTO> getOrdersNeedingAttention();

    /**
     * Get order statistics for admin dashboard
     */
    AdminOrderStatsDTO getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Bulk update order status
     */
    List<AdminOrderDTO> bulkUpdateOrderStatus(List<Long> orderIds, OrderStatus newStatus);

    /**
     * Add admin notes to order
     */
    AdminOrderDTO addAdminNotes(Long orderId, String notes);
}