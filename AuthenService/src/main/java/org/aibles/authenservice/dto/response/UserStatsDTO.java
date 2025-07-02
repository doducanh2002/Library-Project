package org.aibles.authenservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDTO {
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long lockedUsers;
    private Long newUsersThisMonth;
    private Long newUsersThisWeek;
    private Long usersWithMultipleRoles;
    private Double averageUsersPerDay;
}