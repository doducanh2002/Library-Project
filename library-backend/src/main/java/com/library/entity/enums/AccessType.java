package com.library.entity.enums;

/**
 * Enum representing the types of access operations on documents
 */
public enum AccessType {
    /**
     * Document was viewed/previewed in the browser
     */
    VIEW("Document was viewed"),
    
    /**
     * Document was downloaded to user's device
     */
    DOWNLOAD("Document was downloaded"),
    
    /**
     * Document metadata was accessed (e.g., in search results)
     */
    METADATA_ACCESS("Document metadata was accessed"),
    
    /**
     * Access was denied due to insufficient permissions
     */
    ACCESS_DENIED("Access to document was denied");
    
    private final String description;
    
    AccessType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}