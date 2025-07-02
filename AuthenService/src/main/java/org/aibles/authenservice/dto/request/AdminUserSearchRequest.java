package org.aibles.authenservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aibles.authenservice.constan.Gender;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSearchRequest {
    private String keyword; // Search in email, name, username
    private Boolean isActivated;
    private Boolean isLocked;
    private Gender gender;
    private String role;
    private String sortBy = "createdAt"; // email, name, createdAt, lastLoginAt
    private String sortDirection = "desc"; // asc, desc
    private Integer page = 0;
    private Integer size = 20;
}