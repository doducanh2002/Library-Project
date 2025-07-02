package com.library.service;

import com.library.entity.Document;
import com.library.entity.enums.AccessLevel;
import com.library.repository.LoanRepository;
import com.library.repository.OrderItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Slf4j
public class DocumentAccessControlService {
    
    private final LoanRepository loanRepository;
    private final OrderItemRepository orderItemRepository;
    
    public DocumentAccessControlService(LoanRepository loanRepository, 
                                      OrderItemRepository orderItemRepository) {
        this.loanRepository = loanRepository;
        this.orderItemRepository = orderItemRepository;
    }
    
    /**
     * Check if the current user can access a document
     * @param document The document to check access for
     * @return true if user can access, false otherwise
     */
    public boolean canAccessDocument(Document document) {
        if (document == null || !document.getIsActive()) {
            return false;
        }
        
        AccessLevel accessLevel = document.getAccessLevel();
        String currentUserId = getCurrentUserId();
        
        // Public documents are accessible to everyone
        if (accessLevel == AccessLevel.PUBLIC) {
            return true;
        }
        
        // Check if user is authenticated for non-public documents
        if (currentUserId == null) {
            log.debug("Unauthenticated user trying to access non-public document");
            return false;
        }
        
        // Check if user is admin or librarian
        if (isAdminOrLibrarian()) {
            return true;
        }
        
        // Check if user is the uploader for private documents
        if (accessLevel == AccessLevel.PRIVATE) {
            return document.getUploadedBy().equals(currentUserId);
        }
        
        // For logged-in user level, authentication is enough
        if (accessLevel == AccessLevel.LOGGED_IN_USER) {
            return true;
        }
        
        // For restricted by book ownership, check loans and purchases
        if (accessLevel == AccessLevel.RESTRICTED_BY_BOOK_OWNERSHIP) {
            return hasBookAccess(document, currentUserId);
        }
        
        return false;
    }
    
    /**
     * Check if user can upload documents
     * @return true if user can upload
     */
    public boolean canUploadDocuments() {
        return isAdminOrLibrarian();
    }
    
    /**
     * Check if user can manage (update/delete) a document
     * @param document The document to manage
     * @return true if user can manage
     */
    public boolean canManageDocument(Document document) {
        if (document == null) {
            return false;
        }
        
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        
        // Admins and librarians can manage any document
        if (isAdminOrLibrarian()) {
            return true;
        }
        
        // Regular users can only manage their own uploads
        return document.getUploadedBy().equals(currentUserId);
    }
    
    /**
     * Check if user can view document statistics
     * @return true if user can view stats
     */
    public boolean canViewStatistics() {
        return isAdminOrLibrarian();
    }
    
    /**
     * Get the access level a user should have when creating documents
     * @return Default access level for the user
     */
    public AccessLevel getDefaultAccessLevel() {
        if (isAdminOrLibrarian()) {
            return AccessLevel.PUBLIC;
        }
        return AccessLevel.LOGGED_IN_USER;
    }
    
    /**
     * Check if user has access to a book (through loan or purchase)
     * @param document The document linked to a book
     * @param userId The user ID to check
     * @return true if user has book access
     */
    private boolean hasBookAccess(Document document, String userId) {
        if (document.getBook() == null) {
            // Document not linked to any book, so no book-based restriction
            return true;
        }
        
        Long bookId = document.getBook().getId();
        
        // Check if user has an active loan for the book
        boolean hasActiveLoan = loanRepository.existsByUserIdAndBookIdAndStatusIn(
            userId, bookId, 
            java.util.List.of("BORROWED", "APPROVED")
        );
        
        if (hasActiveLoan) {
            log.debug("User {} has active loan for book {}", userId, bookId);
            return true;
        }
        
        // Check if user has purchased the book
        boolean hasPurchased = orderItemRepository.existsByBookIdAndOrderUserIdAndOrderStatusIn(
            bookId, userId,
            java.util.List.of("DELIVERED", "SHIPPED", "PROCESSING", "PAID")
        );
        
        if (hasPurchased) {
            log.debug("User {} has purchased book {}", userId, bookId);
            return true;
        }
        
        return false;
    }
    
    /**
     * Get current authenticated user ID
     * @return User ID or null if not authenticated
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }
    
    /**
     * Check if current user is admin or librarian
     * @return true if user has admin or librarian role
     */
    private boolean isAdminOrLibrarian() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") 
                           || auth.getAuthority().equals("ROLE_LIBRARIAN"));
    }
    
    /**
     * Get current user role
     * @return Role name or null
     */
    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> auth.startsWith("ROLE_"))
            .findFirst()
            .orElse(null);
    }
}