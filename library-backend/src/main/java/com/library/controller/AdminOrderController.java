package com.library.controller;

import com.library.dto.*;
import com.library.entity.OrderStatus;
import com.library.entity.PaymentStatus;
import com.library.service.AdminOrderService;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Order Management", description = "APIs for managing orders by admin/librarian")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all orders", description = "Get paginated list of all orders for admin management")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Page<AdminOrderDTO>> getAllOrders(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting all orders for admin, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminOrderDTO> orders = adminOrderService.getAllOrders(pageable);
        return BaseResponse.success(orders);
    }

    @GetMapping("/status/{status}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get orders by status", description = "Get orders filtered by order status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Page<AdminOrderDTO>> getOrdersByStatus(
            @Parameter(description = "Order status") @PathVariable OrderStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting orders by status: {}, page: {}, size: {}", status, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminOrderDTO> orders = adminOrderService.getOrdersByStatus(status, pageable);
        return BaseResponse.success(orders);
    }

    @GetMapping("/payment-status/{paymentStatus}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get orders by payment status", description = "Get orders filtered by payment status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Page<AdminOrderDTO>> getOrdersByPaymentStatus(
            @Parameter(description = "Payment status") @PathVariable PaymentStatus paymentStatus,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting orders by payment status: {}, page: {}, size: {}", paymentStatus, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminOrderDTO> orders = adminOrderService.getOrdersByPaymentStatus(paymentStatus, pageable);
        return BaseResponse.success(orders);
    }

    @GetMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get order details", description = "Get detailed information about a specific order for admin")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order details retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public BaseResponse<AdminOrderDetailDTO> getOrderDetails(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        
        log.info("Getting order details for admin, orderId: {}", orderId);
        AdminOrderDetailDTO orderDetail = adminOrderService.getOrderDetails(orderId);
        return BaseResponse.success(orderDetail);
    }

    @PutMapping("/{orderId}/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update order status", description = "Update the status of an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public BaseResponse<AdminOrderDTO> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequestDTO request) {
        
        log.info("Updating order status: {} to {}", orderId, request.getNewStatus());
        AdminOrderDTO updatedOrder = adminOrderService.updateOrderStatus(orderId, request);
        return BaseResponse.success(updatedOrder);
    }

    @PutMapping("/{orderId}/payment-status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update payment status", description = "Update the payment status of an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public BaseResponse<AdminOrderDTO> updatePaymentStatus(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody UpdatePaymentStatusRequestDTO request) {
        
        log.info("Updating payment status: {} to {}", orderId, request.getNewPaymentStatus());
        AdminOrderDTO updatedOrder = adminOrderService.updatePaymentStatus(orderId, request);
        return BaseResponse.success(updatedOrder);
    }

    @PostMapping("/{orderId}/refund")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Process refund", description = "Process a refund for an order")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public BaseResponse<AdminOrderDTO> processRefund(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody ProcessRefundRequestDTO request) {
        
        log.info("Processing refund for order: {} amount: {}", orderId, request.getRefundAmount());
        AdminOrderDTO updatedOrder = adminOrderService.processRefund(orderId, request);
        return BaseResponse.success(updatedOrder);
    }

    @GetMapping("/need-attention")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get orders needing attention", description = "Get list of orders that need admin attention")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders needing attention retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<AdminOrderDTO>> getOrdersNeedingAttention() {
        
        log.info("Getting orders needing attention");
        List<AdminOrderDTO> orders = adminOrderService.getOrdersNeedingAttention();
        return BaseResponse.success(orders);
    }

    @GetMapping("/statistics")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get order statistics", description = "Get comprehensive order statistics for admin dashboard")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<AdminOrderStatsDTO> getOrderStatistics(
            @Parameter(description = "Start date (ISO format)") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") @RequestParam(required = false) LocalDateTime endDate) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30); // Default to last 30 days
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        log.info("Getting order statistics from {} to {}", startDate, endDate);
        AdminOrderStatsDTO stats = adminOrderService.getOrderStatistics(startDate, endDate);
        return BaseResponse.success(stats);
    }

    @PutMapping("/bulk-update-status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Bulk update order status", description = "Update status for multiple orders at once")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<AdminOrderDTO>> bulkUpdateOrderStatus(
            @Parameter(description = "Order IDs") @RequestParam List<Long> orderIds,
            @Parameter(description = "New status") @RequestParam OrderStatus newStatus) {
        
        log.info("Bulk updating {} orders to status {}", orderIds.size(), newStatus);
        List<AdminOrderDTO> updatedOrders = adminOrderService.bulkUpdateOrderStatus(orderIds, newStatus);
        return BaseResponse.success(updatedOrders);
    }

    @PostMapping("/{orderId}/notes")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Add admin notes", description = "Add administrative notes to an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Admin notes added successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public BaseResponse<AdminOrderDTO> addAdminNotes(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "Notes to add") @RequestBody String notes) {
        
        log.info("Adding admin notes to order: {}", orderId);
        AdminOrderDTO updatedOrder = adminOrderService.addAdminNotes(orderId, notes);
        return BaseResponse.success(updatedOrder);
    }
}