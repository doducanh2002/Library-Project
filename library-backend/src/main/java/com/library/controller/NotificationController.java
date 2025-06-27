package com.library.controller;

import com.library.dto.*;
import com.library.entity.enums.NotificationStatus;
import com.library.security.RateLimit;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification management APIs")
//@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get user notifications", description = "Get paginated list of user notifications")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BaseResponse<Page<NotificationDTO>> getUserNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Notification status filter") 
            @RequestParam(required = false) NotificationStatus status,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort direction") 
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Getting notifications for user: {}", userDetails.getUsername());
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        
        Page<NotificationDTO> notifications;
        if (status != null) {
            notifications = notificationService.getUserNotificationsByStatus(userDetails.getUsername(), status, pageable);
        } else {
            notifications = notificationService.getUserNotifications(userDetails.getUsername(), pageable);
        }
        
        return BaseResponse.success(notifications);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get notification by ID", description = "Get specific notification details")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BaseResponse<NotificationDTO> getNotificationById(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Notification ID") @PathVariable Long id) {

        log.info("Getting notification {} for user: {}", id, userDetails.getUsername());
        NotificationDTO notification = notificationService.getNotificationById(id, userDetails.getUsername());
        return BaseResponse.success(notification);
    }

    @GetMapping("/recent")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get recent notifications", description = "Get notifications from last few hours")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BaseResponse<List<NotificationDTO>> getRecentNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Hours to look back") 
            @RequestParam(defaultValue = "24") int hours) {

        log.info("Getting recent notifications for user: {} (last {} hours)", userDetails.getUsername(), hours);
        List<NotificationDTO> notifications = notificationService.getRecentNotifications(userDetails.getUsername(), hours);
        return BaseResponse.success(notifications);
    }

    @GetMapping("/high-priority")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get high priority unread notifications", description = "Get unread notifications with high priority")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BaseResponse<List<NotificationDTO>> getHighPriorityNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting high priority notifications for user: {}", userDetails.getUsername());
        List<NotificationDTO> notifications = notificationService.getHighPriorityUnreadNotifications(userDetails.getUsername());
        return BaseResponse.success(notifications);
    }

    @GetMapping("/summary")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get notification summary", description = "Get notification counts and statistics")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BaseResponse<NotificationSummaryDTO> getNotificationSummary(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting notification summary for user: {}", userDetails.getUsername());
        NotificationSummaryDTO summary = notificationService.getNotificationSummary(userDetails.getUsername());
        return BaseResponse.success(summary);
    }

    @GetMapping("/unread-count")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @RateLimit(value = 1000)
    public BaseResponse<Long> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Getting unread count for user: {}", userDetails.getUsername());
        Long count = notificationService.getUnreadCount(userDetails.getUsername());
        return BaseResponse.success(count);
    }

    @PutMapping("/{id}/read")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BaseResponse<NotificationDTO> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Notification ID") @PathVariable Long id) {

        log.info("Marking notification {} as read for user: {}", id, userDetails.getUsername());
        NotificationDTO notification = notificationService.markAsRead(id, userDetails.getUsername());
        return BaseResponse.success(notification);
    }

    @PutMapping("/mark-all-read")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Mark all notifications as read", description = "Mark all user notifications as read")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BaseResponse<String> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Marking all notifications as read for user: {}", userDetails.getUsername());
        int updated = notificationService.markAllAsRead(userDetails.getUsername());
        return BaseResponse.success(String.format("Marked %d notifications as read", updated));
    }

    @PutMapping("/bulk-mark-read")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Bulk mark notifications as read", description = "Mark multiple notifications as read")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BaseResponse<String> bulkMarkAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MarkNotificationRequestDTO request) {

        log.info("Bulk marking {} notifications as read for user: {}", 
                request.getNotificationIds().size(), userDetails.getUsername());
        
        int updated = notificationService.markAsRead(request.getNotificationIds(), userDetails.getUsername());
        return BaseResponse.success(String.format("Marked %d notifications as read", updated));
    }

    @PutMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Archive notification", description = "Archive a specific notification")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BaseResponse<NotificationDTO> markAsArchived(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Notification ID") @PathVariable Long id) {

        log.info("Archiving notification {} for user: {}", id, userDetails.getUsername());
        NotificationDTO notification = notificationService.markAsArchived(id, userDetails.getUsername());
        return BaseResponse.success(notification);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BaseResponse<String> deleteNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Notification ID") @PathVariable Long id) {

        log.info("Deleting notification {} for user: {}", id, userDetails.getUsername());
        boolean deleted = notificationService.deleteNotification(id, userDetails.getUsername());
        
        if (deleted) {
            return BaseResponse.success("Notification deleted successfully");
        } else {
            return BaseResponse.error("Notification not found or cannot be deleted");
        }
    }

    @DeleteMapping("/bulk-delete")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Bulk delete notifications", description = "Delete multiple notifications")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BaseResponse<String> bulkDeleteNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MarkNotificationRequestDTO request) {

        log.info("Bulk deleting {} notifications for user: {}", 
                request.getNotificationIds().size(), userDetails.getUsername());
        
        int deleted = notificationService.bulkDeleteNotifications(request.getNotificationIds(), userDetails.getUsername());
        return BaseResponse.success(String.format("Deleted %d notifications", deleted));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create notification", description = "Create a new notification (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<NotificationDTO> createNotification(@Valid @RequestBody CreateNotificationRequestDTO request) {
        log.info("Creating notification for user: {}, type: {}", request.getUserId(), request.getType());
        NotificationDTO notification = notificationService.createNotification(request);
        return BaseResponse.success(notification);
    }
}