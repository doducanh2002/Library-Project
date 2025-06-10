package com.library.service.impl;

import com.library.dto.*;
import com.library.entity.Book;
import com.library.entity.CartItem;
import com.library.exception.BookNotFoundException;
import com.library.exception.BookNotAvailableException;
import com.library.exception.InsufficientStockException;
import com.library.repository.BookRepository;
import com.library.repository.CartItemRepository;
import com.library.service.BookService;
import com.library.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    
    @Override
    public CartItemDTO addToCart(Long userId, AddToCartRequestDTO request) {
        log.info("Adding item to cart for user: {}, bookId: {}, quantity: {}", 
                userId, request.getBookId(), request.getQuantity());
        
        validateCartItem(userId, request.getBookId(), request.getQuantity());
        
        Book book = getBookById(request.getBookId());
        
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndBookId(userId, request.getBookId());
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            if (newQuantity > book.getStockForSale()) {
                throw new InsufficientStockException("Not enough stock available. Available: " + book.getStockForSale());
            }
            
            cartItem.setQuantity(newQuantity);
            log.info("Updated existing cart item quantity to: {}", newQuantity);
        } else {
            cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setBook(book);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUnitPrice(book.getPrice());
            log.info("Created new cart item");
        }
        
        CartItem savedItem = cartItemRepository.save(cartItem);
        return toCartItemDTO(savedItem);
    }
    
    @Override
    public CartItemDTO updateCartItem(Long userId, Long bookId, UpdateCartItemRequestDTO request) {
        log.info("Updating cart item for user: {}, bookId: {}, new quantity: {}", 
                userId, bookId, request.getQuantity());
        
        CartItem cartItem = cartItemRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new BookNotFoundException("Cart item not found"));
        
        validateCartItem(userId, bookId, request.getQuantity());
        
        cartItem.setQuantity(request.getQuantity());
        CartItem savedItem = cartItemRepository.save(cartItem);
        
        return toCartItemDTO(savedItem);
    }
    
    @Override
    public void removeFromCart(Long userId, Long bookId) {
        log.info("Removing item from cart for user: {}, bookId: {}", userId, bookId);
        
        if (!cartItemRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new BookNotFoundException("Cart item not found");
        }
        
        cartItemRepository.deleteByUserIdAndBookId(userId, bookId);
    }
    
    @Override
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);
        cartItemRepository.deleteByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CartItemDTO> getCartItems(Long userId) {
        log.info("Getting cart items for user: {}", userId);
        
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithBooks(userId);
        return cartItems.stream()
                .map(this::toCartItemDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public CartSummaryDTO getCartSummary(Long userId) {
        log.info("Getting cart summary for user: {}", userId);
        
        List<CartItemDTO> items = getCartItems(userId);
        
        int totalItems = items.size();
        int totalQuantity = items.stream().mapToInt(CartItemDTO::getQuantity).sum();
        BigDecimal totalPrice = items.stream()
                .map(CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<String> validationErrors = new ArrayList<>();
        boolean isValid = true;
        
        for (CartItemDTO item : items) {
            if (!item.getIsAvailable() || item.getQuantity() > item.getAvailableStock()) {
                isValid = false;
                validationErrors.add("Item '" + item.getBookTitle() + "' is not available or has insufficient stock");
            }
        }
        
        return CartSummaryDTO.builder()
                .userId(userId)
                .items(items)
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice)
                .subtotal(totalPrice)
                .tax(BigDecimal.ZERO) // TODO: Implement tax calculation
                .shipping(BigDecimal.ZERO) // TODO: Implement shipping calculation
                .isValid(isValid)
                .validationErrors(validationErrors)
                .canCheckout(isValid && totalItems > 0)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CartItemDTO> getValidCartItems(Long userId) {
        log.info("Getting valid cart items for user: {}", userId);
        
        List<CartItem> cartItems = cartItemRepository.findValidCartItemsByUserId(userId);
        return cartItems.stream()
                .map(this::toCartItemDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CartItemDTO> getInvalidCartItems(Long userId) {
        log.info("Getting invalid cart items for user: {}", userId);
        
        List<CartItem> cartItems = cartItemRepository.findInvalidCartItemsByUserId(userId);
        return cartItems.stream()
                .map(this::toCartItemDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateCartForCheckout(Long userId) {
        log.info("Validating cart for checkout for user: {}", userId);
        
        List<CartItem> invalidItems = cartItemRepository.findInvalidCartItemsByUserId(userId);
        return invalidItems.isEmpty();
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getCartItemCount(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getTotalQuantity(Long userId) {
        Integer total = cartItemRepository.getTotalQuantityByUserId(userId);
        return total != null ? total : 0;
    }
    
    @Override
    public boolean canAddToCart(Long userId, Long bookId, int quantity) {
        try {
            validateCartItem(userId, bookId, quantity);
            return true;
        } catch (Exception e) {
            log.warn("Cannot add to cart: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public void validateCartItem(Long userId, Long bookId, int quantity) {
        Book book = getBookById(bookId);
        
        if (!book.getIsSellable()) {
            throw new BookNotAvailableException("Book is not available for sale");
        }
        
        if (book.getStockForSale() <= 0) {
            throw new InsufficientStockException("Book is out of stock");
        }
        
        // Check existing cart item quantity
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndBookId(userId, bookId);
        int existingQuantity = existingItem.map(CartItem::getQuantity).orElse(0);
        int totalQuantity = existingQuantity + quantity;
        
        if (totalQuantity > book.getStockForSale()) {
            throw new InsufficientStockException(
                String.format("Not enough stock available. Available: %d, Requested: %d", 
                    book.getStockForSale(), totalQuantity));
        }
        
        if (quantity > 50) { // Business rule: max 50 books per item
            throw new IllegalArgumentException("Cannot add more than 50 books per item");
        }
    }
    
    @Override
    public void syncCartWithInventory(Long userId) {
        log.info("Syncing cart with inventory for user: {}", userId);
        
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithBooks(userId);
        
        for (CartItem item : cartItems) {
            Book book = item.getBook();
            
            if (!book.getIsSellable() || book.getStockForSale() <= 0) {
                // Remove unavailable items
                cartItemRepository.delete(item);
                log.info("Removed unavailable item from cart: {}", book.getTitle());
            } else if (item.getQuantity() > book.getStockForSale()) {
                // Adjust quantity to available stock
                item.setQuantity(book.getStockForSale());
                cartItemRepository.save(item);
                log.info("Adjusted quantity for item: {} to {}", book.getTitle(), book.getStockForSale());
            } else if (!item.getUnitPrice().equals(book.getPrice())) {
                // Update price if changed
                item.setUnitPrice(book.getPrice());
                cartItemRepository.save(item);
                log.info("Updated price for item: {} to {}", book.getTitle(), book.getPrice());
            }
        }
    }
    
    private Book getBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));
    }
    
    private CartItemDTO toCartItemDTO(CartItem cartItem) {
        Book book = cartItem.getBook();
        
        // Get authors string
        String authors = book.getBookAuthors().stream()
                .map(ba -> ba.getAuthor().getName())
                .collect(Collectors.joining(", "));
        
        boolean isAvailable = book.getIsSellable() && book.getStockForSale() > 0;
        boolean isValid = isAvailable && cartItem.getQuantity() <= book.getStockForSale();
        
        String validationMessage = null;
        if (!book.getIsSellable()) {
            validationMessage = "Book is not available for sale";
        } else if (book.getStockForSale() <= 0) {
            validationMessage = "Book is out of stock";
        } else if (cartItem.getQuantity() > book.getStockForSale()) {
            validationMessage = "Requested quantity exceeds available stock";
        }
        
        return CartItemDTO.builder()
                .id(cartItem.getId())
                .userId(cartItem.getUserId())
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .bookIsbn(book.getIsbn())
                .bookCoverImageUrl(book.getCoverImageUrl())
                .unitPrice(cartItem.getUnitPrice())
                .quantity(cartItem.getQuantity())
                .totalPrice(cartItem.getTotalPrice())
                .availableStock(book.getStockForSale())
                .isAvailable(isAvailable)
                .authors(authors)
                .publisherName(book.getPublisher() != null ? book.getPublisher().getName() : null)
                .isValid(isValid)
                .validationMessage(validationMessage)
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}