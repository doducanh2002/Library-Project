//package com.library.service;
//
//import com.library.dto.*;
//import com.library.entity.*;
//import com.library.repository.*;
//import com.library.service.impl.OrderServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class OrderServiceTest {
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private OrderItemRepository orderItemRepository;
//
//    @Mock
//    private CartItemRepository cartItemRepository;
//
//    @Mock
//    private BookRepository bookRepository;
//
//    @Mock
//    private CartService cartService;
//
//    @InjectMocks
//    private OrderServiceImpl orderService;
//
//    private Book testBook;
//    private CartItem testCartItem;
//    private CreateOrderRequestDTO createOrderRequest;
//
//    @BeforeEach
//    void setUp() {
//        testBook = Book.builder()
//                .id(1L)
//                .title("Test Book")
//                .isbn("978-0123456789")
//                .price(new BigDecimal("100000"))
//                .isSellable(true)
//                .stockForSale(10)
//                .build();
//
//        testCartItem = CartItem.builder()
//                .id(1L)
//                .userId(1L)
//                .book(testBook)
//                .quantity(2)
//                .unitPrice(new BigDecimal("100000"))
//                .build();
//
//        createOrderRequest = new CreateOrderRequestDTO();
//        createOrderRequest.setPaymentMethod("CREDIT_CARD");
//        createOrderRequest.setShippingAddressLine1("123 Test Street");
//        createOrderRequest.setShippingCity("Test City");
//        createOrderRequest.setShippingPostalCode("12345");
//        createOrderRequest.setShippingCountry("Vietnam");
//    }
//
//    @Test
//    void createOrderFromCart_Success() {
//        // Arrange
//        Long userId = 1L;
//
//        when(cartItemRepository.findByUserId(userId)).thenReturn(List.of(testCartItem));
//
//        CartSummaryDTO cartSummary = CartSummaryDTO.builder()
//                .totalItems(1)
//                .totalPrice(new BigDecimal("200000"))
//                .items(List.of())
//                .build();
//        when(cartService.getCartSummary(userId)).thenReturn(cartSummary);
//
//        Order savedOrder = Order.builder()
//                .id(1L)
//                .userId(userId)
//                .orderCode("ORD-202501-000001")
//                .subTotalAmount(new BigDecimal("200000"))
//                .shippingFee(new BigDecimal("30000"))
//                .taxAmount(new BigDecimal("20000"))
//                .discountAmount(BigDecimal.ZERO)
//                .totalAmount(new BigDecimal("250000"))
//                .status(OrderStatus.PENDING_PAYMENT)
//                .paymentStatus(PaymentStatus.UNPAID)
//                .orderDate(LocalDateTime.now())
//                .build();
//
//        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
//        when(orderRepository.count()).thenReturn(0L);
//
//        // Act
//        OrderDTO result = orderService.createOrderFromCart(userId, createOrderRequest);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(userId, result.getUserId());
//        assertEquals(OrderStatus.PENDING_PAYMENT, result.getStatus());
//        assertEquals(PaymentStatus.UNPAID, result.getPaymentStatus());
//        assertTrue(result.getOrderCode().contains("ORD-"));
//
//        verify(orderRepository).save(any(Order.class));
//        verify(cartItemRepository).deleteByUserId(userId);
//    }
//
//    @Test
//    void createOrderFromCart_EmptyCart() {
//        // Arrange
//        Long userId = 1L;
//        when(cartItemRepository.findByUserId(userId)).thenReturn(List.of());
//
//        // Act & Assert
//        IllegalStateException exception = assertThrows(
//                IllegalStateException.class,
//                () -> orderService.createOrderFromCart(userId, createOrderRequest)
//        );
//
//        assertEquals("Cart is empty", exception.getMessage());
//        verify(orderRepository, never()).save(any());
//    }
//
//    @Test
//    void calculateOrderTotals_Success() {
//        // Arrange
//        Long userId = 1L;
//
//        CartSummaryDTO cartSummary = CartSummaryDTO.builder()
//                .totalItems(2)
//                .totalPrice(new BigDecimal("200000"))
//                .items(List.of())
//                .build();
//
//        when(cartService.getCartSummary(userId)).thenReturn(cartSummary);
//
//        // Act
//        OrderCalculationDTO result = orderService.calculateOrderTotals(userId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(new BigDecimal("200000"), result.getSubTotalAmount());
//        assertEquals(new BigDecimal("30000"), result.getShippingFee()); // Shipping fee for < 500k
//        assertEquals(new BigDecimal("20000"), result.getTaxAmount()); // 10% tax
//        assertEquals(BigDecimal.ZERO, result.getDiscountAmount()); // No discount for < 1M
//        assertTrue(result.getCanProceedToCheckout());
//    }
//
//    @Test
//    void calculateOrderTotals_FreeShipping() {
//        // Arrange
//        Long userId = 1L;
//
//        CartSummaryDTO cartSummary = CartSummaryDTO.builder()
//                .totalItems(1)
//                .totalPrice(new BigDecimal("600000")) // Over 500k for free shipping
//                .items(List.of())
//                .build();
//
//        when(cartService.getCartSummary(userId)).thenReturn(cartSummary);
//
//        // Act
//        OrderCalculationDTO result = orderService.calculateOrderTotals(userId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(new BigDecimal("600000"), result.getSubTotalAmount());
//        assertEquals(BigDecimal.ZERO, result.getShippingFee()); // Free shipping
//        assertEquals(new BigDecimal("60000"), result.getTaxAmount()); // 10% tax
//        assertEquals(BigDecimal.ZERO, result.getDiscountAmount()); // No discount
//    }
//
//    @Test
//    void calculateOrderTotals_WithDiscount() {
//        // Arrange
//        Long userId = 1L;
//
//        CartSummaryDTO cartSummary = CartSummaryDTO.builder()
//                .totalItems(1)
//                .totalPrice(new BigDecimal("1200000")) // Over 1M for discount
//                .items(List.of())
//                .build();
//
//        when(cartService.getCartSummary(userId)).thenReturn(cartSummary);
//
//        // Act
//        OrderCalculationDTO result = orderService.calculateOrderTotals(userId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(new BigDecimal("1200000"), result.getSubTotalAmount());
//        assertEquals(BigDecimal.ZERO, result.getShippingFee()); // Free shipping
//        assertEquals(new BigDecimal("120000"), result.getTaxAmount()); // 10% tax
//        assertEquals(new BigDecimal("60000"), result.getDiscountAmount()); // 5% discount
//    }
//
//    @Test
//    void canUserPlaceOrder_Success() {
//        // Arrange
//        Long userId = 1L;
//        when(orderRepository.countByUserIdAndStatus(userId, OrderStatus.PENDING_PAYMENT))
//                .thenReturn(2L); // Less than 3
//
//        // Act
//        boolean result = orderService.canUserPlaceOrder(userId);
//
//        // Assert
//        assertTrue(result);
//    }
//
//    @Test
//    void canUserPlaceOrder_TooManyPendingOrders() {
//        // Arrange
//        Long userId = 1L;
//        when(orderRepository.countByUserIdAndStatus(userId, OrderStatus.PENDING_PAYMENT))
//                .thenReturn(3L); // Equal to 3
//
//        // Act
//        boolean result = orderService.canUserPlaceOrder(userId);
//
//        // Assert
//        assertFalse(result);
//    }
//
//    @Test
//    void getOrderByCode_Success() {
//        // Arrange
//        String orderCode = "ORD-202501-000001";
//        Long userId = 1L;
//
//        Order order = Order.builder()
//                .id(1L)
//                .userId(userId)
//                .orderCode(orderCode)
//                .subTotalAmount(new BigDecimal("200000"))
//                .totalAmount(new BigDecimal("250000"))
//                .status(OrderStatus.PENDING_PAYMENT)
//                .paymentStatus(PaymentStatus.UNPAID)
//                .orderDate(LocalDateTime.now())
//                .build();
//
//        when(orderRepository.findByOrderCodeAndUserId(orderCode, userId))
//                .thenReturn(Optional.of(order));
//
//        // Act
//        OrderDTO result = orderService.getOrderByCode(orderCode, userId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(orderCode, result.getOrderCode());
//        assertEquals(userId, result.getUserId());
//    }
//
//    @Test
//    void getOrderByCode_NotFound() {
//        // Arrange
//        String orderCode = "ORD-NOTFOUND";
//        Long userId = 1L;
//
//        when(orderRepository.findByOrderCodeAndUserId(orderCode, userId))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        IllegalArgumentException exception = assertThrows(
//                IllegalArgumentException.class,
//                () -> orderService.getOrderByCode(orderCode, userId)
//        );
//
//        assertTrue(exception.getMessage().contains("Order not found"));
//    }
//}