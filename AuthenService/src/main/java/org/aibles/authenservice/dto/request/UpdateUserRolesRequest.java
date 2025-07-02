package org.aibles.authenservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRolesRequest {
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    @NotEmpty(message = "Roles list cannot be empty")
    private List<String> roles;
}