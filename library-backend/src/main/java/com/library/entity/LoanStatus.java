package com.library.entity;

public enum LoanStatus {
    REQUESTED("Requested"),
    APPROVED("Approved"),
    BORROWED("Borrowed"),
    RETURNED("Returned"),
    OVERDUE("Overdue"),
    CANCELLED("Cancelled");

    private final String displayName;

    LoanStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}