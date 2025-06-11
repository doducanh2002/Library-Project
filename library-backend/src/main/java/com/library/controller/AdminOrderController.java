package com.library.controller;

import com.library.dto.*;
import com.library.entity.OrderStatus;
import com.library.entity.PaymentStatus;
import com.library.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Order Management", description = "APIs for managing orders by admin/librarian")
@PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    @Operation(summary = "Get all orders", description = "Get paginated list of all orders for admin management")
    public ResponseEntity<BaseResponse<Page<AdminOrderDTO>>> getAllOrders(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminOrderDTO> orders = adminOrderService.getAllOrders(pageable);
        
        return ResponseEntity.ok(
            BaseResponse.<Page<AdminOrderDTO>>builder()
                .success(true)
                .message("Orders retrieved successfully")
                .data(orders)
                .build()
        );
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status", description = "Get orders filtered by order status")
    public ResponseEntity<BaseResponse<Page<AdminOrderDTO>>> getOrdersByStatus(
            @Parameter(description = "Order status") @PathVariable OrderStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminOrderDTO> orders = adminOrderService.getOrdersByStatus(status, pageable);
        
        return ResponseEntity.ok(
            BaseResponse.<Page<AdminOrderDTO>>builder()
                .success(true)
                .message("Orders retrieved successfully")
                .data(orders)
                .build()
        );
    }

    @GetMapping("/payment-status/{paymentStatus}")
    @Operation(summary = "Get orders by payment status", description = "Get orders filtered by payment status")
    public ResponseEntity<BaseResponse<Page<AdminOrderDTO>>> getOrdersByPaymentStatus(
            @Parameter(description = "Payment status") @PathVariable PaymentStatus paymentStatus,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminOrderDTO> orders = adminOrderService.getOrdersByPaymentStatus(paymentStatus, pageable);
        
        return ResponseEntity.ok(
            BaseResponse.<Page<AdminOrderDTO>>builder()
                .success(true)
                .message("Orders retrieved successfully")
                .data(orders)
                .build()
        );
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details", description = "Get detailed information about a specific order for admin")
    public ResponseEntity<BaseResponse<AdminOrderDetailDTO>> getOrderDetails(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        
        AdminOrderDetailDTO orderDetail = adminOrderService.getOrderDetails(orderId);
        
        return ResponseEntity.ok(
            BaseResponse.<AdminOrderDetailDTO>builder()
                .success(true)
                .message("Order details retrieved successfully")
                .data(orderDetail)
                .build()
        );
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update the status of an order")
    public ResponseEntity<BaseResponse<AdminOrderDTO>> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequestDTO request) {
        
        log.info("Updating order status: {} to {}", orderId, request.getNewStatus());
        AdminOrderDTO updatedOrder = adminOrderService.updateOrderStatus(orderId, request);
        
        return ResponseEntity.ok(
            BaseResponse.<AdminOrderDTO>builder()
                .success(true)
                .message("Order status updated successfully")
                .data(updatedOrder)
                .build()
        );
    }

    @PutMapping("/{orderId}/payment-status")
    @Operation(summary = "Update payment status", description = "Update the payment status of an order")
    public ResponseEntity<BaseResponse<AdminOrderDTO>> updatePaymentStatus(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody UpdatePaymentStatusRequestDTO request) {
        
        log.info("Updating payment status: {} to {}", orderId, request.getNewPaymentStatus());
        AdminOrderDTO updatedOrder = adminOrderService.updatePaymentStatus(orderId, request);
        
        return ResponseEntity.ok(
            BaseResponse.<AdminOrderDTO>builder()
                .success(true)
                .message("Payment status updated successfully")
                .data(updatedOrder)
                .build()
        );
    }

    @PostMapping("/{orderId}/refund")
    @Operation(summary = "Process refund", description = "Process a refund for an order")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<AdminOrderDTO>> processRefund(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody ProcessRefundRequestDTO request) {
        
        log.info("Processing refund for order: {} amount: {}", orderId, request.getRefundAmount());
        AdminOrderDTO updatedOrder = adminOrderService.processRefund(orderId, request);
        
        return ResponseEntity.ok(
            BaseResponse.<AdminOrderDTO>builder()
                .success(true)
                .message("Refund processed successfully")
                .data(updatedOrder)
                .build()
        );
    }

    @GetMapping("/need-attention")
    @Operation(summary = "Get orders needing attention", description = "Get list of orders that need admin attention")
    public ResponseEntity<BaseResponse<List<AdminOrderDTO>>> getOrdersNeedingAttention() {
        
        List<AdminOrderDTO> orders = adminOrderService.getOrdersNeedingAttention();
        
        return ResponseEntity.ok(
            BaseResponse.<List<AdminOrderDTO>>builder()
                .success(true)
                .message("Orders needing attention retrieved successfully")
                .data(orders)
                .build()
        );
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get order statistics", description = "Get comprehensive order statistics for admin dashboard")
    public ResponseEntity<BaseResponse<AdminOrderStatsDTO>> getOrderStatistics(
            @Parameter(description = "Start date (ISO format)") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") @RequestParam(required = false) LocalDateTime endDate) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30); // Default to last 30 days
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        AdminOrderStatsDTO stats = adminOrderService.getOrderStatistics(startDate, endDate);
        
        return ResponseEntity.ok(
            BaseResponse.<AdminOrderStatsDTO>builder()
                .success(true)
                .message("Order statistics retrieved successfully")
                .data(stats)
                .build()
        );
    }

    @PutMapping("/bulk-update-status")
    @Operation(summary = "Bulk update order status", description = "Update status for multiple orders at once")
    public ResponseEntity<BaseResponse<List<AdminOrderDTO>>> bulkUpdateOrderStatus(
            @Parameter(description = "Order IDs") @RequestParam List<Long> orderIds,
            @Parameter(description = "New status") @RequestParam OrderStatus newStatus) {
        
        log.info("Bulk updating {} orders to status {}", orderIds.size(), newStatus);
        List<AdminOrderDTO> updatedOrders = adminOrderService.bulkUpdateOrderStatus(orderIds, newStatus);
        
        return ResponseEntity.ok(
            BaseResponse.<List<AdminOrderDTO>>builder()
                .success(true)
                .message(String.format("Successfully updated %d orders", updatedOrders.size()))
                .data(updatedOrders)
                .build()
        );
    }

    @PostMapping("/{orderId}/notes")
    @Operation(summary = "Add admin notes", description = "Add administrative notes to an order")
    public ResponseEntity<BaseResponse<AdminOrderDTO>> addAdminNotes(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "Notes to add") @RequestBody String notes) {
        
        AdminOrderDTO updatedOrder = adminOrderService.addAdminNotes(orderId, notes);
        
        return ResponseEntity.ok(
            BaseResponse.<AdminOrderDTO>builder()
                .success(true)
                .message("Admin notes added successfully")
                .data(updatedOrder)
                .build()
        );
    }
}