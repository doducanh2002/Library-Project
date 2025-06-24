package com.library.controller;

import com.library.dto.BaseResponse;
import com.library.dto.CreateNotificationRequestDTO;
import com.library.dto.NotificationDTO;
import com.library.entity.enums.NotificationStatus;
import com.library.entity.enums.NotificationType;
import com.library.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Notifications", description = "Admin notification management APIs")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
public class AdminNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all notifications", description = "Get paginated list of all notifications (admin)")
    public BaseResponse<Page<NotificationDTO>> getAllNotifications(
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort direction") 
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Admin getting all notifications");
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        
        Page<NotificationDTO> notifications = notificationService.getAllNotifications(pageable);
        return BaseResponse.success(notifications);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search notifications", description = "Search notifications with multiple criteria")
    public BaseResponse<Page<NotificationDTO>> searchNotifications(
            @Parameter(description = "User ID filter") 
            @RequestParam(required = false) String userId,
            @Parameter(description = "Status filter") 
            @RequestParam(required = false) NotificationStatus status,
            @Parameter(description = "Type filter") 
            @RequestParam(required = false) NotificationType type,
            @Parameter(description = "Priority filter") 
            @RequestParam(required = false) Integer priority,
            @Parameter(description = "Start date filter") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            @Parameter(description = "End date filter") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime until,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort direction") 
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Admin searching notifications with criteria");
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        
        Page<NotificationDTO> notifications = notificationService.searchNotifications(
                userId, status, type, priority, since, until, pageable);
        
        return BaseResponse.success(notifications);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create notification", description = "Create a new notification for any user")
    public BaseResponse<NotificationDTO> createNotification(@Valid @RequestBody CreateNotificationRequestDTO request) {
        log.info("Admin creating notification for user: {}, type: {}", request.getUserId(), request.getType());
        NotificationDTO notification = notificationService.createNotification(request);
        return BaseResponse.success(notification);
    }

    @PostMapping("/broadcast")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Broadcast notification", description = "Send notification to multiple users")
    public BaseResponse<String> broadcastNotification(
            @Parameter(description = "User IDs to send notification to") 
            @RequestParam List<String> userIds,
            @Valid @RequestBody CreateNotificationRequestDTO request) {

        log.info("Admin broadcasting notification to {} users", userIds.size());
        
        int created = 0;
        for (String userId : userIds) {
            try {
                CreateNotificationRequestDTO userRequest = CreateNotificationRequestDTO.builder()
                        .userId(userId)
                        .type(request.getType())
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .referenceId(request.getReferenceId())
                        .referenceType(request.getReferenceType())
                        .actionUrl(request.getActionUrl())
                        .priority(request.getPriority())
                        .sendEmail(request.getSendEmail())
                        .expiresAt(request.getExpiresAt())
                        .metadata(request.getMetadata())
                        .build();
                
                notificationService.createNotification(userRequest);
                created++;
            } catch (Exception e) {
                log.error("Failed to create notification for user: {}", userId, e);
            }
        }
        
        return BaseResponse.success(String.format("Notification sent to %d out of %d users", created, userIds.size()));
    }

    @PostMapping("/system-maintenance")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send system maintenance notification", description = "Send maintenance notification to all users")
    public BaseResponse<String> sendMaintenanceNotification(
            @Parameter(description = "Maintenance message") 
            @RequestParam String message,
            @Parameter(description = "Scheduled maintenance time") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledTime) {

        log.info("Admin sending system maintenance notification");
        notificationService.sendSystemMaintenanceNotification(message, scheduledTime);
        return BaseResponse.success("System maintenance notification sent");
    }

    @GetMapping("/email-pending")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get notifications needing email", description = "Get notifications that need email sending")
    public BaseResponse<List<NotificationDTO>> getNotificationsNeedingEmail(
            @Parameter(description = "Minimum priority for email sending") 
            @RequestParam(defaultValue = "2") Integer minPriority) {

        log.info("Admin getting notifications needing email with priority >= {}", minPriority);
        List<NotificationDTO> notifications = notificationService.getNotificationsNeedingEmail(minPriority);
        return BaseResponse.success(notifications);
    }

    @PutMapping("/{id}/email-sent")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Mark email as sent", description = "Mark that email has been sent for notification")
    public BaseResponse<String> markEmailSent(
            @Parameter(description = "Notification ID") @PathVariable Long id) {

        log.info("Admin marking email as sent for notification: {}", id);
        notificationService.markEmailSent(id);
        return BaseResponse.success("Email status updated");
    }

    @PostMapping("/cleanup-expired")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cleanup expired notifications", description = "Archive expired notifications")
    public BaseResponse<String> cleanupExpiredNotifications() {
        log.info("Admin triggering cleanup of expired notifications");
        notificationService.cleanupExpiredNotifications();
        return BaseResponse.success("Expired notifications cleaned up");
    }

    @PostMapping("/cleanup-old")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cleanup old notifications", description = "Delete notifications older than specified days")
    public BaseResponse<String> cleanupOldNotifications(
            @Parameter(description = "Days old to consider for cleanup") 
            @RequestParam(defaultValue = "90") int daysOld) {

        log.info("Admin triggering cleanup of notifications older than {} days", daysOld);
        notificationService.cleanupOldNotifications(daysOld);
        return BaseResponse.success(String.format("Notifications older than %d days cleaned up", daysOld));
    }

    @GetMapping("/statistics")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get notification statistics", description = "Get system-wide notification statistics")
    public BaseResponse<Object> getNotificationStatistics() {
        log.info("Admin getting notification statistics");
        
        // This would need to be implemented in the service to get system-wide stats
        // For now, return a placeholder
        return BaseResponse.success("Notification statistics endpoint - to be implemented");
    }
}