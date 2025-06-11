package com.library.service.impl;

import com.library.dto.*;
import com.library.entity.*;
import com.library.exception.BookNotAvailableException;
import com.library.exception.InsufficientStockException;
import com.library.repository.*;
import com.library.service.CartService;
import com.library.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final CartService cartService;

    @Override
    @Transactional
    public OrderDTO createOrderFromCart(Long userId, CreateOrderRequestDTO request) {
        log.info("Creating order from cart for user: {}", userId);

        // Get user's cart items
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByCreatedAtAsc(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Validate cart items and stock
        validateCartForOrder(cartItems);

        // Calculate order totals
        OrderCalculationDTO calculation = calculateOrderTotals(userId);
        if (!calculation.isCanProceedToCheckout()) {
            throw new IllegalStateException("Cannot proceed to checkout: " + 
                String.join(", ", calculation.getValidationErrors()));
        }

        // Create order
        Order order = Order.builder()
                .userId(userId)
                .orderCode(generateOrderCode())
                .subTotalAmount(calculation.getSubTotalAmount())
                .shippingFee(calculation.getShippingFee())
                .discountAmount(calculation.getDiscountAmount())
                .taxAmount(calculation.getTaxAmount())
                .totalAmount(calculation.getTotalAmount())
                .status(OrderStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.UNPAID)
                .paymentMethod(request.getPaymentMethod())
                .shippingAddressLine1(request.getShippingAddressLine1())
                .shippingAddressLine2(request.getShippingAddressLine2())
                .shippingCity(request.getShippingCity())
                .shippingPostalCode(request.getShippingPostalCode())
                .shippingCountry(request.getShippingCountry())
                .customerNote(request.getCustomerNote())
                .build();

        order = orderRepository.save(order);

        // Create order items and update book stock
        for (CartItem cartItem : cartItems) {
            // Update book stock
            Book book = cartItem.getBook();
            if (book.getStockForSale() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for book: " + book.getTitle());
            }
            book.setStockForSale(book.getStockForSale() - cartItem.getQuantity());
            bookRepository.save(book);

            // Create order item
            OrderItem orderItem = OrderItem.fromCartItem(cartItem, order);
            orderItem.calculateItemTotalPrice();
            order.addOrderItem(orderItem);
        }

        // Save order with items
        order = orderRepository.save(order);

        // Clear user's cart
        cartItemRepository.deleteByUserId(userId);

        log.info("Order created successfully: {}", order.getOrderCode());
        return convertToDTO(order);
    }

    @Override
    public Page<OrderSummaryDTO> getUserOrderHistory(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return orders.map(this::convertToSummaryDTO);
    }

    @Override
    public OrderDTO getOrderByCode(String orderCode, Long userId) {
        Order order = orderRepository.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));
        return convertToDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(String orderCode, Long userId) {
        Order order = orderRepository.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Cannot cancel paid order. Please contact support for refund.");
        }

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order that has been shipped or delivered");
        }

        // Restore book stock
        for (OrderItem item : order.getOrderItems()) {
            Book book = item.getBook();
            book.setStockForSale(book.getStockForSale() + item.getQuantity());
            bookRepository.save(book);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        log.info("Order cancelled: {}", orderCode);
        return convertToDTO(order);
    }

    @Override
    public List<OrderSummaryDTO> getUserCurrentOrders(Long userId) {
        List<OrderStatus> currentStatuses = List.of(
                OrderStatus.PENDING_PAYMENT,
                OrderStatus.PAID,
                OrderStatus.PROCESSING,
                OrderStatus.SHIPPED
        );

        List<Order> orders = new ArrayList<>();
        for (OrderStatus status : currentStatuses) {
            orders.addAll(orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status));
        }

        return orders.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean canUserPlaceOrder(Long userId) {
        // Check if user has any pending payment orders
        long pendingOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.PENDING_PAYMENT);
        return pendingOrders < 3; // Allow max 3 pending orders
    }

    @Override
    public OrderCalculationDTO calculateOrderTotals(Long userId) {
        CartSummaryDTO cartSummary = cartService.getCartSummary(userId);
        
        if (cartSummary.getTotalItems() == 0) {
            return OrderCalculationDTO.builder()
                    .canProceedToCheckout(false)
                    .validationErrors(List.of("Cart is empty"))
                    .build();
        }

        List<String> validationErrors = new ArrayList<>();
        
        // Validate stock availability
        for (CartItemDTO item : cartSummary.getItems()) {
            if (item.getBook().getStockForSale() < item.getQuantity()) {
                validationErrors.add("Insufficient stock for: " + item.getBook().getTitle());
            }
            if (!item.getBook().getIsSellable()) {
                validationErrors.add("Book not available for sale: " + item.getBook().getTitle());
            }
        }

        BigDecimal subTotal = cartSummary.getTotalPrice();
        BigDecimal shippingFee = calculateShippingFee(subTotal);
        BigDecimal tax = calculateTax(subTotal);
        BigDecimal discount = calculateDiscount(subTotal, userId);
        BigDecimal total = subTotal.add(shippingFee).add(tax).subtract(discount);

        return OrderCalculationDTO.builder()
                .subTotalAmount(subTotal)
                .shippingFee(shippingFee)
                .taxAmount(tax)
                .discountAmount(discount)
                .totalAmount(total)
                .totalItems(cartSummary.getTotalItems())
                .cartItems(cartSummary.getItems())
                .canProceedToCheckout(validationErrors.isEmpty())
                .validationErrors(validationErrors)
                .build();
    }

    private void validateCartForOrder(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            Book book = item.getBook();
            if (!book.getIsSellable()) {
                throw new BookNotAvailableException("Book not available for sale: " + book.getTitle());
            }
            if (book.getStockForSale() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for book: " + book.getTitle());
            }
        }
    }

    private String generateOrderCode() {
        String prefix = "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = orderRepository.count() + 1;
        return prefix + "-" + String.format("%06d", count);
    }

    private BigDecimal calculateShippingFee(BigDecimal subTotal) {
        // Free shipping for orders over 500,000 VND
        if (subTotal.compareTo(new BigDecimal("500000")) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("30000"); // 30,000 VND shipping fee
    }

    private BigDecimal calculateTax(BigDecimal subTotal) {
        // 10% VAT
        return subTotal.multiply(new BigDecimal("0.10"));
    }

    private BigDecimal calculateDiscount(BigDecimal subTotal, Long userId) {
        // Simple discount logic - 5% for orders over 1,000,000 VND
        if (subTotal.compareTo(new BigDecimal("1000000")) >= 0) {
            return subTotal.multiply(new BigDecimal("0.05"));
        }
        return BigDecimal.ZERO;
    }

    private OrderDTO convertToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(this::convertOrderItemToDTO)
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .orderDate(order.getOrderDate())
                .subTotalAmount(order.getSubTotalAmount())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .shippingAddressLine1(order.getShippingAddressLine1())
                .shippingAddressLine2(order.getShippingAddressLine2())
                .shippingCity(order.getShippingCity())
                .shippingPostalCode(order.getShippingPostalCode())
                .shippingCountry(order.getShippingCountry())
                .shippingDate(order.getShippingDate())
                .deliveryDate(order.getDeliveryDate())
                .customerNote(order.getCustomerNote())
                .totalItems(itemDTOs.size())
                .orderItems(itemDTOs)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderSummaryDTO convertToSummaryDTO(Order order) {
        return OrderSummaryDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .totalItems(order.getOrderItems().size())
                .shippingCity(order.getShippingCity())
                .expectedDeliveryDate(calculateExpectedDeliveryDate(order))
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderItemDTO convertOrderItemToDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .bookId(item.getBook().getId())
                .quantity(item.getQuantity())
                .pricePerUnit(item.getPricePerUnit())
                .itemTotalPrice(item.getItemTotalPrice())
                .bookTitle(item.getBookTitle())
                .bookIsbn(item.getBookIsbn())
                .bookCoverImageUrl(item.getBook().getCoverImageUrl())
                .bookDescription(item.getBook().getDescription())
                .bookStillAvailable(item.getBook().getIsSellable() && item.getBook().getStockForSale() > 0)
                .build();
    }

    private LocalDateTime calculateExpectedDeliveryDate(Order order) {
        if (order.getDeliveryDate() != null) {
            return order.getDeliveryDate();
        }
        if (order.getShippingDate() != null) {
            return order.getShippingDate().plusDays(3); // 3 days after shipping
        }
        return order.getOrderDate().plusDays(7); // 7 days from order date
    }

    @Override
    public Page<OrderHistoryDTO> getDetailedOrderHistory(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return orders.map(this::convertToHistoryDTO);
    }

    @Override
    public UserOrderStatsDTO getUserOrderStatistics(Long userId) {
        List<Order> allOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged()).getContent();
        
        if (allOrders.isEmpty()) {
            return UserOrderStatsDTO.builder()
                    .totalOrders(0L)
                    .canPlaceNewOrder(true)
                    .build();
        }

        long totalOrders = allOrders.size();
        long completedOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count();
        long cancelledOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                .count();
        long pendingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING_PAYMENT || 
                           o.getStatus() == OrderStatus.PAID || 
                           o.getStatus() == OrderStatus.PROCESSING)
                .count();

        BigDecimal totalSpent = allOrders.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = totalSpent.divide(BigDecimal.valueOf(Math.max(1, completedOrders)), 2, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal largestOrderValue = allOrders.stream()
                .map(Order::getTotalAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        int totalBooksPurchased = allOrders.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .mapToInt(o -> o.getOrderItems().stream()
                        .mapToInt(OrderItem::getQuantity)
                        .sum())
                .sum();

        return UserOrderStatsDTO.builder()
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .pendingOrders(pendingOrders)
                .totalSpent(totalSpent)
                .averageOrderValue(averageOrderValue)
                .largestOrderValue(largestOrderValue)
                .totalBooksPurchased(totalBooksPurchased)
                .firstOrderDate(allOrders.get(allOrders.size() - 1).getOrderDate())
                .lastOrderDate(allOrders.get(0).getOrderDate())
                .activeOrders((int) pendingOrders)
                .canPlaceNewOrder(canUserPlaceOrder(userId))
                .build();
    }

    @Override
    public OrderCalculationDTO reorderFromExistingOrder(String orderCode, Long userId) {
        Order existingOrder = orderRepository.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));

        // Clear current cart
        cartItemRepository.deleteByUserId(userId);

        // Add items from existing order to cart
        for (OrderItem item : existingOrder.getOrderItems()) {
            Book book = item.getBook();
            if (book.getIsSellable() && book.getStockForSale() > 0) {
                CartItem cartItem = CartItem.builder()
                        .userId(userId)
                        .book(book)
                        .quantity(Math.min(item.getQuantity(), book.getStockForSale()))
                        .build();
                cartItemRepository.save(cartItem);
            }
        }

        // Return calculation for the new cart
        return calculateOrderTotals(userId);
    }

    @Override
    public OrderTrackingDTO trackOrder(String orderCode, Long userId) {
        Order order = orderRepository.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));

        List<OrderTrackingDTO.OrderTrackingEvent> timeline = buildOrderTimeline(order);
        
        return OrderTrackingDTO.builder()
                .orderCode(order.getOrderCode())
                .currentStatus(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .timeline(timeline)
                .currentStepDescription(getStatusDescription(order.getStatus()))
                .nextStepDescription(getNextStepDescription(order.getStatus()))
                .estimatedNextUpdate(calculateNextUpdateTime(order))
                .shippingAddress(buildShippingAddress(order))
                .estimatedDelivery(calculateExpectedDeliveryDate(order))
                .progressPercentage(calculateProgressPercentage(order.getStatus()))
                .canBeCancelled(canOrderBeCancelled(order))
                .build();
    }

    private OrderHistoryDTO convertToHistoryDTO(Order order) {
        List<OrderItemDTO> itemsPreview = order.getOrderItems().stream()
                .limit(3) // Show first 3 items as preview
                .map(this::convertOrderItemToDTO)
                .collect(Collectors.toList());

        return OrderHistoryDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .totalItems(order.getOrderItems().size())
                .totalBooks(order.getOrderItems().stream()
                        .mapToInt(OrderItem::getQuantity)
                        .sum())
                .shippingDate(order.getShippingDate())
                .deliveryDate(order.getDeliveryDate())
                .shippingCity(order.getShippingCity())
                .statusDescription(getStatusDescription(order.getStatus()))
                .progressPercentage(calculateProgressPercentage(order.getStatus()))
                .nextAction(getNextAction(order))
                .itemsPreview(itemsPreview)
                .createdAt(order.getCreatedAt())
                .canBeCancelled(canOrderBeCancelled(order))
                .canBeReordered(order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED)
                .build();
    }

    private List<OrderTrackingDTO.OrderTrackingEvent> buildOrderTimeline(Order order) {
        List<OrderTrackingDTO.OrderTrackingEvent> events = new ArrayList<>();
        
        // Order placed
        events.add(OrderTrackingDTO.OrderTrackingEvent.builder()
                .status("ORDER_PLACED")
                .description("Order placed successfully")
                .timestamp(order.getOrderDate())
                .isCompleted(true)
                .build());

        // Payment
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            events.add(OrderTrackingDTO.OrderTrackingEvent.builder()
                    .status("PAYMENT_CONFIRMED")
                    .description("Payment confirmed")
                    .timestamp(order.getOrderDate().plusHours(1)) // Assuming payment within 1 hour
                    .isCompleted(true)
                    .build());
        }

        // Processing
        if (order.getStatus().ordinal() >= OrderStatus.PROCESSING.ordinal()) {
            events.add(OrderTrackingDTO.OrderTrackingEvent.builder()
                    .status("PROCESSING")
                    .description("Order is being processed")
                    .timestamp(order.getOrderDate().plusHours(2))
                    .isCompleted(true)
                    .build());
        }

        // Shipped
        if (order.getShippingDate() != null) {
            events.add(OrderTrackingDTO.OrderTrackingEvent.builder()
                    .status("SHIPPED")
                    .description("Order has been shipped")
                    .timestamp(order.getShippingDate())
                    .isCompleted(true)
                    .build());
        }

        // Delivered
        if (order.getDeliveryDate() != null) {
            events.add(OrderTrackingDTO.OrderTrackingEvent.builder()
                    .status("DELIVERED")
                    .description("Order delivered successfully")
                    .timestamp(order.getDeliveryDate())
                    .isCompleted(true)
                    .build());
        }

        return events;
    }

    private String getStatusDescription(OrderStatus status) {
        return switch (status) {
            case PENDING_PAYMENT -> "Waiting for payment";
            case PAID -> "Payment received, preparing for processing";
            case PROCESSING -> "Order is being prepared";
            case SHIPPED -> "Order has been shipped";
            case DELIVERED -> "Order delivered successfully";
            case CANCELLED -> "Order has been cancelled";
            case REFUNDED -> "Order has been refunded";
        };
    }

    private String getNextStepDescription(OrderStatus status) {
        return switch (status) {
            case PENDING_PAYMENT -> "Complete payment to proceed";
            case PAID -> "Order will be processed soon";
            case PROCESSING -> "Order will be shipped soon";
            case SHIPPED -> "Order will be delivered soon";
            case DELIVERED -> "Order completed";
            case CANCELLED -> "Order cancelled";
            case REFUNDED -> "Order refunded";
        };
    }

    private String getNextAction(Order order) {
        return switch (order.getStatus()) {
            case PENDING_PAYMENT -> "Pay Now";
            case PAID, PROCESSING -> "Track Order";
            case SHIPPED -> "Track Delivery";
            case DELIVERED -> "Rate & Review";
            case CANCELLED -> "Reorder";
            case REFUNDED -> "Reorder";
        };
    }

    private Integer calculateProgressPercentage(OrderStatus status) {
        return switch (status) {
            case PENDING_PAYMENT -> 10;
            case PAID -> 25;
            case PROCESSING -> 50;
            case SHIPPED -> 75;
            case DELIVERED -> 100;
            case CANCELLED, REFUNDED -> 0;
        };
    }

    private boolean canOrderBeCancelled(Order order) {
        return order.getStatus() == OrderStatus.PENDING_PAYMENT || 
               (order.getStatus() == OrderStatus.PAID && order.getShippingDate() == null);
    }

    private LocalDateTime calculateNextUpdateTime(Order order) {
        return switch (order.getStatus()) {
            case PENDING_PAYMENT -> order.getOrderDate().plusHours(24); // 24 hours to pay
            case PAID -> order.getOrderDate().plusDays(1); // Process within 1 day
            case PROCESSING -> order.getOrderDate().plusDays(2); // Ship within 2 days
            case SHIPPED -> order.getShippingDate().plusDays(3); // Deliver within 3 days
            default -> null;
        };
    }

    private String buildShippingAddress(Order order) {
        StringBuilder address = new StringBuilder();
        if (order.getShippingAddressLine1() != null) {
            address.append(order.getShippingAddressLine1());
        }
        if (order.getShippingAddressLine2() != null) {
            address.append(", ").append(order.getShippingAddressLine2());
        }
        if (order.getShippingCity() != null) {
            address.append(", ").append(order.getShippingCity());
        }
        if (order.getShippingPostalCode() != null) {
            address.append(" ").append(order.getShippingPostalCode());
        }
        return address.toString();
    }
}