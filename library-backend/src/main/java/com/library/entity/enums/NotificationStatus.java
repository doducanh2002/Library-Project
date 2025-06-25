package com.library.entity.enums;

public enum NotificationStatus {
    UNREAD("Unread"),
    READ("Read"),
    ARCHIVED("Archived");
    
    private final String displayName;
    
    NotificationStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}