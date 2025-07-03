package org.aibles.authenservice.dto.request;

import jakarta.validation.constraints.NotNull;

public class UpdateUserStatusRequest {
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    private Boolean isActivated;
    private Boolean isLocked;
    private String reason;

    public UpdateUserStatusRequest() {
    }

    public UpdateUserStatusRequest(String userId, Boolean isActivated, Boolean isLocked, String reason) {
        this.userId = userId;
        this.isActivated = isActivated;
        this.isLocked = isLocked;
        this.reason = reason;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getActivated() {
        return isActivated;
    }

    public void setActivated(Boolean activated) {
        isActivated = activated;
    }

    public Boolean getLocked() {
        return isLocked;
    }

    public void setLocked(Boolean locked) {
        isLocked = locked;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}