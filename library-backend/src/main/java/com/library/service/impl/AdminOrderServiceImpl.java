package com.library.service.impl;

import com.library.dto.*;
import com.library.entity.*;
import com.library.repository.*;
import com.library.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookRepository bookRepository;

    @Override
    public Page<AdminOrderDTO> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::convertToAdminDTO);
    }

    @Override
    public Page<AdminOrderDTO> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return orders.map(this::convertToAdminDTO);
    }

    @Override
    public Page<AdminOrderDTO> getOrdersByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable) {
        Page<Order> orders = orderRepository.findByPaymentStatusOrderByCreatedAtDesc(paymentStatus, pageable);
        return orders.map(this::convertToAdminDTO);
    }

    @Override
    public AdminOrderDetailDTO getOrderDetails(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return convertToAdminDetailDTO(order);
    }

    @Override
    @Transactional
    public AdminOrderDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequestDTO request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getNewStatus());

        // Update specific fields based on new status
        switch (request.getNewStatus()) {
            case SHIPPED:
                if (request.getShippingDate() != null) {
                    order.setShippingDate(request.getShippingDate());
                } else {
                    order.setShippingDate(LocalDateTime.now());
                }
                break;
            case DELIVERED:
                if (request.getDeliveryDate() != null) {
                    order.setDeliveryDate(request.getDeliveryDate());
                } else {
                    order.setDeliveryDate(LocalDateTime.now());
                }
                break;
            case CANCELLED:
                // Restore stock if order is cancelled
                restoreStockForOrder(order);
                break;
        }

        // Add admin notes
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            String existingNotes = order.getAdminNotes() != null ? order.getAdminNotes() : "";
            String newNotes = String.format("[%s] Status changed from %s to %s: %s", 
                    LocalDateTime.now(), oldStatus, request.getNewStatus(), request.getNotes());
            order.setAdminNotes(existingNotes.isEmpty() ? newNotes : existingNotes + "\n" + newNotes);
        }

        order = orderRepository.save(order);
        log.info("Order status updated: {} from {} to {}", order.getOrderCode(), oldStatus, request.getNewStatus());
        
        return convertToAdminDTO(order);
    }

    @Override
    @Transactional
    public AdminOrderDTO updatePaymentStatus(Long orderId, UpdatePaymentStatusRequestDTO request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        PaymentStatus oldStatus = order.getPaymentStatus();
        order.setPaymentStatus(request.getNewPaymentStatus());

        if (request.getTransactionId() != null) {
            order.setPaymentTransactionId(request.getTransactionId());
        }

        // Auto-update order status based on payment status
        if (request.getNewPaymentStatus() == PaymentStatus.PAID && order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            order.setStatus(OrderStatus.PAID);
        }

        // Add admin notes
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            String existingNotes = order.getAdminNotes() != null ? order.getAdminNotes() : "";
            String newNotes = String.format("[%s] Payment status changed from %s to %s: %s", 
                    LocalDateTime.now(), oldStatus, request.getNewPaymentStatus(), request.getNotes());
            order.setAdminNotes(existingNotes.isEmpty() ? newNotes : existingNotes + "\n" + newNotes);
        }

        order = orderRepository.save(order);
        log.info("Payment status updated: {} from {} to {}", order.getOrderCode(), oldStatus, request.getNewPaymentStatus());
        
        return convertToAdminDTO(order);
    }

    @Override
    @Transactional
    public AdminOrderDTO processRefund(Long orderId, ProcessRefundRequestDTO request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Cannot refund order that is not paid");
        }

        // Update payment status
        if (request.getRefundAmount().compareTo(order.getTotalAmount()) >= 0) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            order.setStatus(OrderStatus.REFUNDED);
        } else {
            order.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }

        // Restore stock if requested
        if (request.isRestoreStock()) {
            restoreStockForOrder(order);
        }

        // Add admin notes
        String refundNotes = String.format("[%s] Refund processed: %s VND. Reason: %s", 
                LocalDateTime.now(), request.getRefundAmount(), request.getReason());
        String existingNotes = order.getAdminNotes() != null ? order.getAdminNotes() : "";
        order.setAdminNotes(existingNotes.isEmpty() ? refundNotes : existingNotes + "\n" + refundNotes);

        order = orderRepository.save(order);
        log.info("Refund processed for order: {} amount: {}", order.getOrderCode(), request.getRefundAmount());
        
        return convertToAdminDTO(order);
    }

    @Override
    public List<AdminOrderDTO> getOrdersNeedingAttention() {
        List<OrderStatus> needAttentionStatuses = Arrays.asList(
                OrderStatus.PAID, OrderStatus.PROCESSING
        );
        
        List<Order> orders = orderRepository.findRecentOrdersByStatus(needAttentionStatuses, PageRequest.of(0, 50));
        
        return orders.stream()
                .map(this::convertToAdminDTO)
                .filter(AdminOrderDTO::isRequiresAction)
                .collect(Collectors.toList());
    }

    @Override
    public AdminOrderStatsDTO getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> ordersInPeriod = orderRepository.findByOrderDateBetweenOrderByCreatedAtDesc(
                startDate, endDate, Pageable.unpaged()).getContent();

        Map<OrderStatus, Long> statusCounts = ordersInPeriod.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        BigDecimal totalRevenue = ordersInPeriod.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefunds = ordersInPeriod.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.REFUNDED || 
                           o.getPaymentStatus() == PaymentStatus.PARTIALLY_REFUNDED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long paidOrdersCount = statusCounts.getOrDefault(OrderStatus.DELIVERED, 0L) + 
                              statusCounts.getOrDefault(OrderStatus.SHIPPED, 0L) +
                              statusCounts.getOrDefault(OrderStatus.PROCESSING, 0L);

        BigDecimal avgOrderValue = paidOrdersCount > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(paidOrdersCount), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;

        // Calculate processing metrics
        double avgProcessingTime = ordersInPeriod.stream()
                .filter(o -> o.getShippingDate() != null)
                .mapToDouble(o -> ChronoUnit.HOURS.between(o.getOrderDate(), o.getShippingDate()))
                .average()
                .orElse(0.0);

        // Top selling books
        Map<String, Integer> topSellingBooks = getTopSellingBooks(ordersInPeriod, 10);

        return AdminOrderStatsDTO.builder()
                .totalOrders((long) ordersInPeriod.size())
                .pendingPaymentOrders(statusCounts.getOrDefault(OrderStatus.PENDING_PAYMENT, 0L))
                .paidOrders(statusCounts.getOrDefault(OrderStatus.PAID, 0L))
                .processingOrders(statusCounts.getOrDefault(OrderStatus.PROCESSING, 0L))
                .shippedOrders(statusCounts.getOrDefault(OrderStatus.SHIPPED, 0L))
                .deliveredOrders(statusCounts.getOrDefault(OrderStatus.DELIVERED, 0L))
                .cancelledOrders(statusCounts.getOrDefault(OrderStatus.CANCELLED, 0L))
                .refundedOrders(statusCounts.getOrDefault(OrderStatus.REFUNDED, 0L))
                .totalRevenue(totalRevenue)
                .totalRefunds(totalRefunds)
                .netRevenue(totalRevenue.subtract(totalRefunds))
                .averageOrderValue(avgOrderValue)
                .averageProcessingTimeHours(avgProcessingTime)
                .topSellingBooks(topSellingBooks)
                .ordersNeedingAttention((long) getOrdersNeedingAttention().size())
                .build();
    }

    @Override
    @Transactional
    public List<AdminOrderDTO> bulkUpdateOrderStatus(List<Long> orderIds, OrderStatus newStatus) {
        List<Order> orders = orderRepository.findAllById(orderIds);
        
        for (Order order : orders) {
            order.setStatus(newStatus);
            if (newStatus == OrderStatus.SHIPPED && order.getShippingDate() == null) {
                order.setShippingDate(LocalDateTime.now());
            }
            if (newStatus == OrderStatus.DELIVERED && order.getDeliveryDate() == null) {
                order.setDeliveryDate(LocalDateTime.now());
            }
        }
        
        List<Order> updatedOrders = orderRepository.saveAll(orders);
        log.info("Bulk updated {} orders to status {}", updatedOrders.size(), newStatus);
        
        return updatedOrders.stream()
                .map(this::convertToAdminDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminOrderDTO addAdminNotes(Long orderId, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        String timestampedNotes = String.format("[%s] %s", LocalDateTime.now(), notes);
        String existingNotes = order.getAdminNotes() != null ? order.getAdminNotes() : "";
        order.setAdminNotes(existingNotes.isEmpty() ? timestampedNotes : existingNotes + "\n" + timestampedNotes);

        order = orderRepository.save(order);
        return convertToAdminDTO(order);
    }

    private void restoreStockForOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Book book = item.getBook();
            book.setStockForSale(book.getStockForSale() + item.getQuantity());
            bookRepository.save(book);
        }
        log.info("Stock restored for cancelled/refunded order: {}", order.getOrderCode());
    }

    private Map<String, Integer> getTopSellingBooks(List<Order> orders, int limit) {
        return orders.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .flatMap(o -> o.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getBookTitle(),
                        Collectors.summingInt(OrderItem::getQuantity)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private AdminOrderDTO convertToAdminDTO(Order order) {
        long daysSinceOrder = ChronoUnit.DAYS.between(order.getOrderDate(), LocalDateTime.now());
        
        boolean requiresAction = determineIfRequiresAction(order);
        String actionRequired = getActionRequired(order);
        int priority = calculatePriority(order);

        return AdminOrderDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .orderDate(order.getOrderDate())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .totalItems(order.getOrderItems().size())
                .totalBooks(order.getOrderItems().stream().mapToInt(OrderItem::getQuantity).sum())
                .shippingCity(order.getShippingCity())
                .shippingDate(order.getShippingDate())
                .deliveryDate(order.getDeliveryDate())
                .requiresAction(requiresAction)
                .actionRequired(actionRequired)
                .priority(priority)
                .daysSinceOrder((int) daysSinceOrder)
                .customerNote(order.getCustomerNote())
                .adminNotes(order.getAdminNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private AdminOrderDetailDTO convertToAdminDetailDTO(Order order) {
        List<AdminOrderDetailDTO.AdminOrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(this::convertToAdminOrderItemDTO)
                .collect(Collectors.toList());

        List<String> availableActions = getAvailableActions(order);
        List<AdminOrderDetailDTO.OrderTimelineEventDTO> timeline = buildAdminTimeline(order);

        return AdminOrderDetailDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .orderDate(order.getOrderDate())
                .userId(order.getUserId())
                .subTotalAmount(order.getSubTotalAmount())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentTransactionId(order.getPaymentTransactionId())
                .shippingAddressLine1(order.getShippingAddressLine1())
                .shippingAddressLine2(order.getShippingAddressLine2())
                .shippingCity(order.getShippingCity())
                .shippingPostalCode(order.getShippingPostalCode())
                .shippingCountry(order.getShippingCountry())
                .shippingDate(order.getShippingDate())
                .deliveryDate(order.getDeliveryDate())
                .orderItems(itemDTOs)
                .customerNote(order.getCustomerNote())
                .adminNotes(order.getAdminNotes())
                .availableActions(availableActions)
                .canUpdateStatus(canUpdateStatus(order))
                .canRefund(canRefund(order))
                .canCancel(canCancel(order))
                .timeline(timeline)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private AdminOrderDetailDTO.AdminOrderItemDTO convertToAdminOrderItemDTO(OrderItem item) {
        return AdminOrderDetailDTO.AdminOrderItemDTO.builder()
                .id(item.getId())
                .bookId(item.getBook().getId())
                .bookTitle(item.getBookTitle())
                .bookIsbn(item.getBookIsbn())
                .quantity(item.getQuantity())
                .pricePerUnit(item.getPricePerUnit())
                .itemTotalPrice(item.getItemTotalPrice())
                .currentStock(item.getBook().getStockForSale())
                .bookStillExists(true) // Assuming book still exists if we can access it
                .build();
    }

    private boolean determineIfRequiresAction(Order order) {
        long hoursSinceOrder = ChronoUnit.HOURS.between(order.getOrderDate(), LocalDateTime.now());
        
        return switch (order.getStatus()) {
            case PENDING_PAYMENT -> hoursSinceOrder > 24; // Pending payment for more than 24 hours
            case PAID -> hoursSinceOrder > 48; // Paid but not processed for more than 48 hours
            case PROCESSING -> hoursSinceOrder > 72; // Processing for more than 72 hours
            default -> false;
        };
    }

    private String getActionRequired(Order order) {
        if (!determineIfRequiresAction(order)) {
            return null;
        }
        
        return switch (order.getStatus()) {
            case PENDING_PAYMENT -> "Follow up on payment";
            case PAID -> "Process order";
            case PROCESSING -> "Ship order";
            default -> null;
        };
    }

    private int calculatePriority(Order order) {
        long hoursSinceOrder = ChronoUnit.HOURS.between(order.getOrderDate(), LocalDateTime.now());
        
        if (hoursSinceOrder > 72) return 1; // High priority
        if (hoursSinceOrder > 48) return 2; // Medium priority
        return 3; // Low priority
    }

    private List<String> getAvailableActions(Order order) {
        List<String> actions = new ArrayList<>();
        
        if (canUpdateStatus(order)) actions.add("UPDATE_STATUS");
        if (canRefund(order)) actions.add("REFUND");
        if (canCancel(order)) actions.add("CANCEL");
        actions.add("ADD_NOTES");
        
        return actions;
    }

    private boolean canUpdateStatus(Order order) {
        return order.getStatus() != OrderStatus.DELIVERED && 
               order.getStatus() != OrderStatus.CANCELLED && 
               order.getStatus() != OrderStatus.REFUNDED;
    }

    private boolean canRefund(Order order) {
        return order.getPaymentStatus() == PaymentStatus.PAID;
    }

    private boolean canCancel(Order order) {
        return order.getStatus() == OrderStatus.PENDING_PAYMENT || 
               order.getStatus() == OrderStatus.PAID;
    }

    private List<AdminOrderDetailDTO.OrderTimelineEventDTO> buildAdminTimeline(Order order) {
        List<AdminOrderDetailDTO.OrderTimelineEventDTO> events = new ArrayList<>();
        
        events.add(AdminOrderDetailDTO.OrderTimelineEventDTO.builder()
                .event("ORDER_CREATED")
                .description("Order created by customer")
                .timestamp(order.getOrderDate())
                .performedBy("Customer")
                .build());

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            events.add(AdminOrderDetailDTO.OrderTimelineEventDTO.builder()
                    .event("PAYMENT_CONFIRMED")
                    .description("Payment confirmed")
                    .timestamp(order.getOrderDate().plusMinutes(30)) // Estimated
                    .performedBy("Payment System")
                    .build());
        }

        if (order.getShippingDate() != null) {
            events.add(AdminOrderDetailDTO.OrderTimelineEventDTO.builder()
                    .event("ORDER_SHIPPED")
                    .description("Order shipped to customer")
                    .timestamp(order.getShippingDate())
                    .performedBy("Admin")
                    .build());
        }

        if (order.getDeliveryDate() != null) {
            events.add(AdminOrderDetailDTO.OrderTimelineEventDTO.builder()
                    .event("ORDER_DELIVERED")
                    .description("Order delivered successfully")
                    .timestamp(order.getDeliveryDate())
                    .performedBy("Delivery Service")
                    .build());
        }

        return events;
    }
}