package com.library.service;

import com.library.dto.*;
import com.library.entity.*;
import com.library.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.liquibase.enabled=false"
})
@Transactional
@Rollback
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private Long testUserId = 1L;
    private Book testBook;
    private Category testCategory;
    private Author testAuthor;
    private Publisher testPublisher;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        cartItemRepository.deleteAll();
        orderRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        authorRepository.deleteAll();
        publisherRepository.deleteAll();

        // Create test data
        testCategory = Category.builder()
                .name("Test Category")
                .description("Test Category Description")
                .build();
        testCategory = categoryRepository.save(testCategory);

        testAuthor = Author.builder()
                .fullName("Test Author")
                .biography("Test Author Biography")
                .build();
        testAuthor = authorRepository.save(testAuthor);

        testPublisher = Publisher.builder()
                .name("Test Publisher")
                .address("Test Publisher Address")
                .email("test@publisher.com")
                .foundedYear(2000)
                .build();
        testPublisher = publisherRepository.save(testPublisher);

        testBook = Book.builder()
                .title("Test Book for Orders")
                .isbn("978-0123456789")
                .description("A comprehensive test book for order functionality")
                .price(new BigDecimal("150000"))
                .isSellable(true)
                .stockForSale(20)
                .isLendable(true)
                .availableCopiesForLoan(5)
                .totalCopies(25)
                .category(testCategory)
                .publisher(testPublisher)
                .publicationYear(2023)
                .language("Vietnamese")
                .pageCount(300)
                .build();
        testBook = bookRepository.save(testBook);
    }

    @Test
    void completeOrderWorkflow_Success() {
        // Step 1: Add items to cart
        AddToCartRequestDTO addToCartRequest = new AddToCartRequestDTO();
        addToCartRequest.setBookId(testBook.getId());
        addToCartRequest.setQuantity(3);

        CartItemDTO cartItem = cartService.addToCart(testUserId, addToCartRequest);
        assertNotNull(cartItem);
        assertEquals(3, cartItem.getQuantity());

        // Step 2: Get cart summary
        CartSummaryDTO cartSummary = cartService.getCartSummary(testUserId);
        assertNotNull(cartSummary);
        assertEquals(1, cartSummary.getTotalItems());
        assertEquals(new BigDecimal("450000"), cartSummary.getTotalPrice()); // 3 * 150000

        // Step 3: Calculate order totals
        OrderCalculationDTO calculation = orderService.calculateOrderTotals(testUserId);
        assertNotNull(calculation);
        assertTrue(calculation.getCanProceedToCheckout());
        assertEquals(new BigDecimal("450000"), calculation.getSubTotalAmount());
        assertEquals(BigDecimal.ZERO, calculation.getShippingFee()); // Free shipping over 500k
        assertEquals(new BigDecimal("45000"), calculation.getTaxAmount()); // 10% tax

        // Step 4: Create order
        CreateOrderRequestDTO orderRequest = new CreateOrderRequestDTO();
        orderRequest.setPaymentMethod("CREDIT_CARD");
        orderRequest.setShippingAddressLine1("123 Integration Test Street");
        orderRequest.setShippingCity("Test City");
        orderRequest.setShippingPostalCode("12345");
        orderRequest.setShippingCountry("Vietnam");
        orderRequest.setCustomerNote("Integration test order");

        OrderDTO createdOrder = orderService.createOrderFromCart(testUserId, orderRequest);
        assertNotNull(createdOrder);
        assertEquals(testUserId, createdOrder.getUserId());
        assertEquals(OrderStatus.PENDING_PAYMENT, createdOrder.getStatus());
        assertEquals(PaymentStatus.UNPAID, createdOrder.getPaymentStatus());
        assertTrue(createdOrder.getOrderCode().contains("ORD-"));

        // Step 5: Verify cart is cleared
        CartSummaryDTO emptyCart = cartService.getCartSummary(testUserId);
        assertEquals(0, emptyCart.getTotalItems());

        // Step 6: Verify book stock is updated
        Optional<Book> updatedBook = bookRepository.findById(testBook.getId());
        assertTrue(updatedBook.isPresent());
        assertEquals(17, updatedBook.get().getStockForSale()); // 20 - 3

        // Step 7: Get order details
        OrderDTO orderDetails = orderService.getOrderByCode(createdOrder.getOrderCode(), testUserId);
        assertNotNull(orderDetails);
        assertEquals(createdOrder.getOrderCode(), orderDetails.getOrderCode());
        assertEquals(1, orderDetails.getOrderItems().size());

        // Step 8: Get user order history
        Page<OrderSummaryDTO> orderHistory = orderService.getUserOrderHistory(testUserId, PageRequest.of(0, 10));
        assertNotNull(orderHistory);
        assertEquals(1, orderHistory.getTotalElements());
        assertEquals(createdOrder.getOrderCode(), orderHistory.getContent().get(0).getOrderCode());
    }

    @Test
    void orderCalculations_DifferentScenarios() {
        // Scenario 1: Small order (with shipping fee)
        AddToCartRequestDTO smallOrderRequest = new AddToCartRequestDTO();
        smallOrderRequest.setBookId(testBook.getId());
        smallOrderRequest.setQuantity(1); // 150,000 VND

        cartService.addToCart(testUserId, smallOrderRequest);
        OrderCalculationDTO smallOrderCalc = orderService.calculateOrderTotals(testUserId);

        assertEquals(new BigDecimal("150000"), smallOrderCalc.getSubTotalAmount());
        assertEquals(new BigDecimal("30000"), smallOrderCalc.getShippingFee()); // Shipping fee
        assertEquals(new BigDecimal("15000"), smallOrderCalc.getTaxAmount()); // 10% tax
        assertEquals(BigDecimal.ZERO, smallOrderCalc.getDiscountAmount()); // No discount

        // Clear cart for next scenario
        cartService.clearCart(testUserId);

        // Scenario 2: Large order (free shipping + discount)
        AddToCartRequestDTO largeOrderRequest = new AddToCartRequestDTO();
        largeOrderRequest.setBookId(testBook.getId());
        largeOrderRequest.setQuantity(7); // 1,050,000 VND (over 1M for discount)

        cartService.addToCart(testUserId, largeOrderRequest);
        OrderCalculationDTO largeOrderCalc = orderService.calculateOrderTotals(testUserId);

        assertEquals(new BigDecimal("1050000"), largeOrderCalc.getSubTotalAmount());
        assertEquals(BigDecimal.ZERO, largeOrderCalc.getShippingFee()); // Free shipping
        assertEquals(new BigDecimal("105000"), largeOrderCalc.getTaxAmount()); // 10% tax
        assertEquals(new BigDecimal("52500"), largeOrderCalc.getDiscountAmount()); // 5% discount
    }

    @Test
    void orderValidation_InsufficientStock() {
        // Try to add more items than available stock
        AddToCartRequestDTO request = new AddToCartRequestDTO();
        request.setBookId(testBook.getId());
        request.setQuantity(25); // More than available stock (20)

        // This should be caught during order creation, not cart addition
        cartService.addToCart(testUserId, request);

        // Manually reduce stock to simulate race condition
        testBook.setStockForSale(2);
        bookRepository.save(testBook);

        CreateOrderRequestDTO orderRequest = new CreateOrderRequestDTO();
        orderRequest.setPaymentMethod("CREDIT_CARD");
        orderRequest.setShippingAddressLine1("123 Test Street");

        // Should throw exception due to insufficient stock
        assertThrows(Exception.class, () -> {
            orderService.createOrderFromCart(testUserId, orderRequest);
        });
    }

    @Test
    void multipleUsersOrders_Success() {
        Long user1 = 1L;
        Long user2 = 2L;

        // User 1 places order
        AddToCartRequestDTO user1Request = new AddToCartRequestDTO();
        user1Request.setBookId(testBook.getId());
        user1Request.setQuantity(2);
        cartService.addToCart(user1, user1Request);

        CreateOrderRequestDTO orderRequest1 = new CreateOrderRequestDTO();
        orderRequest1.setPaymentMethod("CREDIT_CARD");
        orderRequest1.setShippingAddressLine1("User 1 Address");

        OrderDTO order1 = orderService.createOrderFromCart(user1, orderRequest1);

        // User 2 places order
        AddToCartRequestDTO user2Request = new AddToCartRequestDTO();
        user2Request.setBookId(testBook.getId());
        user2Request.setQuantity(3);
        cartService.addToCart(user2, user2Request);

        CreateOrderRequestDTO orderRequest2 = new CreateOrderRequestDTO();
        orderRequest2.setPaymentMethod("BANK_TRANSFER");
        orderRequest2.setShippingAddressLine1("User 2 Address");

        OrderDTO order2 = orderService.createOrderFromCart(user2, orderRequest2);

        // Verify both orders exist and are separate
        assertNotNull(order1);
        assertNotNull(order2);
        assertNotEquals(order1.getOrderCode(), order2.getOrderCode());
        assertEquals(user1, order1.getUserId());
        assertEquals(user2, order2.getUserId());

        // Verify stock is properly reduced
        Optional<Book> updatedBook = bookRepository.findById(testBook.getId());
        assertTrue(updatedBook.isPresent());
        assertEquals(15, updatedBook.get().getStockForSale()); // 20 - 2 - 3

        // Verify user-specific order history
        Page<OrderSummaryDTO> user1History = orderService.getUserOrderHistory(user1, PageRequest.of(0, 10));
        Page<OrderSummaryDTO> user2History = orderService.getUserOrderHistory(user2, PageRequest.of(0, 10));

        assertEquals(1, user1History.getTotalElements());
        assertEquals(1, user2History.getTotalElements());
        assertEquals(order1.getOrderCode(), user1History.getContent().get(0).getOrderCode());
        assertEquals(order2.getOrderCode(), user2History.getContent().get(0).getOrderCode());
    }

    @Test
    void orderUserLimits_Success() {
        // Test that user can place multiple orders up to limit
        assertTrue(orderService.canUserPlaceOrder(testUserId));

        // Create 3 pending orders (which is the limit)
        for (int i = 0; i < 3; i++) {
            AddToCartRequestDTO request = new AddToCartRequestDTO();
            request.setBookId(testBook.getId());
            request.setQuantity(1);
            cartService.addToCart(testUserId, request);

            CreateOrderRequestDTO orderRequest = new CreateOrderRequestDTO();
            orderRequest.setPaymentMethod("CREDIT_CARD");
            orderRequest.setShippingAddressLine1("Test Address " + i);

            orderService.createOrderFromCart(testUserId, orderRequest);
        }

        // Now user should not be able to place more orders
        assertFalse(orderService.canUserPlaceOrder(testUserId));
    }
}