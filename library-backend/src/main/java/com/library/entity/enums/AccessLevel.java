package com.library.entity.enums;

/**
 * Enum representing the access levels for documents in the library system.
 * These levels determine who can access and download specific documents.
 */
public enum AccessLevel {
    /**
     * Document is publicly accessible to everyone, including anonymous users
     */
    PUBLIC("Anyone can access this document"),
    
    /**
     * Document is accessible only to logged-in users
     */
    LOGGED_IN_USER("Only authenticated users can access this document"),
    
    /**
     * Document is accessible only to users who have borrowed or purchased the associated book
     */
    RESTRICTED_BY_BOOK_OWNERSHIP("Only users who own or have borrowed the book can access this document"),
    
    /**
     * Document is private and only accessible to administrators and the uploader
     */
    PRIVATE("Only administrators and the uploader can access this document");
    
    private final String description;
    
    AccessLevel(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if the access level requires user authentication
     * @return true if authentication is required
     */
    public boolean requiresAuthentication() {
        return this != PUBLIC;
    }
    
    /**
     * Check if the access level requires book ownership verification
     * @return true if book ownership check is required
     */
    public boolean requiresBookOwnership() {
        return this == RESTRICTED_BY_BOOK_OWNERSHIP;
    }
    
    /**
     * Check if the access level is restricted to administrators only
     * @return true if admin access is required
     */
    public boolean isAdminOnly() {
        return this == PRIVATE;
    }
}