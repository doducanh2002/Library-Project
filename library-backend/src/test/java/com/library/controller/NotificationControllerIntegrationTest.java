//package com.library.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.library.dto.CreateNotificationRequestDTO;
//import com.library.dto.MarkNotificationRequestDTO;
//import com.library.entity.enums.NotificationType;
//import com.library.service.NotificationService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Arrays;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureWebMvc
//@ActiveProfiles("test")
//@Transactional
//class NotificationControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private NotificationService notificationService;
//
//    private final String TEST_USER_ID = "test-user";
//
//    @BeforeEach
//    void setUp() {
//        // Create some test notifications
//        createTestNotification("Test Notification 1", NotificationType.LOAN_APPROVED);
//        createTestNotification("Test Notification 2", NotificationType.ORDER_CONFIRMED);
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"USER"})
//    void getUserNotifications_ShouldReturnPaginatedResults() throws Exception {
//        mockMvc.perform(get("/api/v1/notifications")
//                .param("page", "0")
//                .param("size", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.data.content").isArray())
//                .andExpect(jsonPath("$.data.totalElements").value(2));
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"USER"})
//    void getUserNotifications_WithStatusFilter_ShouldFilterCorrectly() throws Exception {
//        mockMvc.perform(get("/api/v1/notifications")
//                .param("status", "UNREAD")
//                .param("page", "0")
//                .param("size", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.data.content").isArray());
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"USER"})
//    void getNotificationSummary_ShouldReturnSummary() throws Exception {
//        mockMvc.perform(get("/api/v1/notifications/summary"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.data.totalNotifications").exists())
//                .andExpect(jsonPath("$.data.unreadCount").exists());
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"USER"})
//    void getUnreadCount_ShouldReturnCount() throws Exception {
//        mockMvc.perform(get("/api/v1/notifications/unread-count"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.data").isNumber());
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"USER"})
//    void getRecentNotifications_ShouldReturnRecentOnes() throws Exception {
//        mockMvc.perform(get("/api/v1/notifications/recent")
//                .param("hours", "24"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.data").isArray());
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"USER"})
//    void getHighPriorityNotifications_ShouldReturnHighPriorityOnes() throws Exception {
//        // Create a high priority notification first
//        createTestNotificationWithPriority("Critical Notification", NotificationType.LOAN_OVERDUE, 4);
//
//        mockMvc.perform(get("/api/v1/notifications/high-priority"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.data").isArray());
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"USER"})
//    void markAllAsRead_ShouldMarkAllNotifications() throws Exception {
//        mockMvc.perform(put("/api/v1/notifications/mark-all-read"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.data").value("Marked 2 notifications as read"));
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"USER"})
//    void bulkMarkAsRead_ShouldMarkSpecifiedNotifications() throws Exception {
//        // Get notification IDs first
//        var notifications = notificationService.getUserNotifications(TEST_USER_ID,
//                org.springframework.data.domain.PageRequest.of(0, 2));
//
//        var notificationIds = notifications.getContent().stream()
//                .map(n -> n.getId())
//                .toList();
//
//        MarkNotificationRequestDTO request = MarkNotificationRequestDTO.builder()
//                .notificationIds(notificationIds)
//                .action("read")
//                .build();
//
//        mockMvc.perform(put("/api/v1/notifications/bulk-mark-read")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"));
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"USER"})
//    void bulkDeleteNotifications_ShouldDeleteSpecifiedNotifications() throws Exception {
//        // Get notification IDs first
//        var notifications = notificationService.getUserNotifications(TEST_USER_ID,
//                org.springframework.data.domain.PageRequest.of(0, 1));
//
//        var notificationIds = notifications.getContent().stream()
//                .map(n -> n.getId())
//                .toList();
//
//        MarkNotificationRequestDTO request = MarkNotificationRequestDTO.builder()
//                .notificationIds(notificationIds)
//                .build();
//
//        mockMvc.perform(delete("/api/v1/notifications/bulk-delete")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"));
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void createNotification_AsAdmin_ShouldCreateSuccessfully() throws Exception {
//        CreateNotificationRequestDTO request = CreateNotificationRequestDTO.builder()
//                .userId("target-user")
//                .type(NotificationType.SYSTEM_MAINTENANCE)
//                .title("System Maintenance")
//                .message("System will be down for maintenance")
//                .priority(3)
//                .build();
//
//        mockMvc.perform(post("/api/v1/notifications")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.data.title").value("System Maintenance"));
//    }
//
//    @Test
//    @WithMockUser(username = "user", roles = {"USER"})
//    void createNotification_AsUser_ShouldBeForbidden() throws Exception {
//        CreateNotificationRequestDTO request = CreateNotificationRequestDTO.builder()
//                .userId("target-user")
//                .type(NotificationType.GENERAL)
//                .title("Test")
//                .message("Test message")
//                .build();
//
//        mockMvc.perform(post("/api/v1/notifications")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void getUserNotifications_WithoutAuth_ShouldBeUnauthorized() throws Exception {
//        mockMvc.perform(get("/api/v1/notifications"))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"USER"})
//    void markNotificationAsRead_ShouldUpdateStatus() throws Exception {
//        // Get a notification ID first
//        var notifications = notificationService.getUserNotifications(TEST_USER_ID,
//                org.springframework.data.domain.PageRequest.of(0, 1));
//        Long notificationId = notifications.getContent().get(0).getId();
//
//        mockMvc.perform(put("/api/v1/notifications/{id}/read", notificationId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.data.status").value("READ"));
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"USER"})
//    void markNotificationAsArchived_ShouldUpdateStatus() throws Exception {
//        // Get a notification ID first
//        var notifications = notificationService.getUserNotifications(TEST_USER_ID,
//                org.springframework.data.domain.PageRequest.of(0, 1));
//        Long notificationId = notifications.getContent().get(0).getId();
//
//        mockMvc.perform(put("/api/v1/notifications/{id}/archive", notificationId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"USER"})
//    void deleteNotification_ShouldRemoveNotification() throws Exception {
//        // Get a notification ID first
//        var notifications = notificationService.getUserNotifications(TEST_USER_ID,
//                org.springframework.data.domain.PageRequest.of(0, 1));
//        Long notificationId = notifications.getContent().get(0).getId();
//
//        mockMvc.perform(delete("/api/v1/notifications/{id}", notificationId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.data").value("Notification deleted successfully"));
//    }
//
//    private void createTestNotification(String title, NotificationType type) {
//        CreateNotificationRequestDTO request = CreateNotificationRequestDTO.builder()
//                .userId(TEST_USER_ID)
//                .type(type)
//                .title(title)
//                .message("Test message for " + title)
//                .priority(1)
//                .build();
//        notificationService.createNotification(request);
//    }
//
//    private void createTestNotificationWithPriority(String title, NotificationType type, Integer priority) {
//        CreateNotificationRequestDTO request = CreateNotificationRequestDTO.builder()
//                .userId(TEST_USER_ID)
//                .type(type)
//                .title(title)
//                .message("Test message for " + title)
//                .priority(priority)
//                .build();
//        notificationService.createNotification(request);
//    }
//}