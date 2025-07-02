package org.aibles.authenservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    private Boolean isActivated;
    private Boolean isLocked;
    private String reason;
}