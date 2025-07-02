package com.library.service.impl;

import com.library.dto.DashboardOverviewDTO;
import com.library.dto.DashboardStatisticsDTO;
import com.library.dto.SystemConfigDTO;
import com.library.repository.*;
import com.library.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;
    private final DocumentAccessLogRepository documentAccessLogRepository;
    private final RestTemplate restTemplate;

    @Value("${app.authen-service.url:http://localhost:8081}")
    private String authenServiceUrl;

    @Override
    @Cacheable(value = "dashboard-overview", unless = "#result == null")
    @Transactional(readOnly = true)
    public DashboardOverviewDTO getDashboardOverview() {
        log.info("Generating dashboard overview");
        
        // Get user statistics from AuthenService
        Map<String, Long> userStats = getUserStatisticsFromAuthenService();
        
        // Book statistics
        Long totalBooks = bookRepository.count();
        Long availableBooks = bookRepository.countByAvailableCopiesGreaterThan(0);
        Long lentBooks = loanRepository.countByStatus("BORROWED");
        Long booksAddedThisMonth = bookRepository.countBooksAddedInCurrentMonth();
        
        // Loan statistics
        Long totalLoans = loanRepository.count();
        Long activeLoans = loanRepository.countByStatusIn(List.of("APPROVED", "BORROWED"));
        Long overdueLoans = loanRepository.countByStatus("OVERDUE");
        Long loansToday = loanRepository.countByCreatedAtAfter(LocalDateTime.now().toLocalDate().atStartOfDay());
        
        // Order statistics
        Long totalOrders = orderRepository.count();
        Long pendingOrders = orderRepository.countByOrderStatus("PENDING_PAYMENT");
        Long completedOrdersToday = orderRepository.countByOrderStatusAndCreatedAtAfter(
            "DELIVERED", LocalDateTime.now().toLocalDate().atStartOfDay());
        BigDecimal revenueToday = orderRepository.getTotalRevenueByDate(LocalDate.now());
        BigDecimal revenueThisMonth = orderRepository.getTotalRevenueByMonth(
            LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        
        // Document statistics
        Long totalDocuments = getDocumentCount();
        Long documentsUploadedThisMonth = getDocumentsUploadedThisMonth();
        Long totalDownloads = documentAccessLogRepository.countByAccessType("DOWNLOAD");
        Long downloadsToday = documentAccessLogRepository.countByAccessTypeAndAccessTimeAfter(
            "DOWNLOAD", LocalDateTime.now().toLocalDate().atStartOfDay());
        
        // Notification statistics
        Long totalNotifications = notificationRepository.count();
        Long unreadNotifications = notificationRepository.countByStatus("UNREAD");
        
        return DashboardOverviewDTO.builder()
            .totalUsers(userStats.getOrDefault("totalUsers", 0L))
            .activeUsers(userStats.getOrDefault("activeUsers", 0L))
            .newUsersToday(userStats.getOrDefault("newUsersToday", 0L))
            .newUsersThisWeek(userStats.getOrDefault("newUsersThisWeek", 0L))
            .totalBooks(totalBooks)
            .availableBooks(availableBooks)
            .lentBooks(lentBooks)
            .booksAddedThisMonth(booksAddedThisMonth)
            .totalLoans(totalLoans)
            .activeLoans(activeLoans)
            .overdueLoans(overdueLoans)
            .loansToday(loansToday)
            .totalOrders(totalOrders)
            .pendingOrders(pendingOrders)
            .completedOrdersToday(completedOrdersToday)
            .revenueToday(revenueToday != null ? revenueToday : BigDecimal.ZERO)
            .revenueThisMonth(revenueThisMonth != null ? revenueThisMonth : BigDecimal.ZERO)
            .totalDocuments(totalDocuments)
            .documentsUploadedThisMonth(documentsUploadedThisMonth)
            .totalDownloads(totalDownloads)
            .downloadsToday(downloadsToday)
            .totalNotifications(totalNotifications)
            .unreadNotifications(unreadNotifications)
            .lastUpdated(LocalDateTime.now())
            .build();
    }

    @Override
    @Cacheable(value = "dashboard-statistics", key = "#startDate.toString() + '_' + #endDate.toString()")
    @Transactional(readOnly = true)
    public DashboardStatisticsDTO getDashboardStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("Generating dashboard statistics from {} to {}", startDate, endDate);
        
        // Generate daily stats
        List<DashboardStatisticsDTO.DailyStatsDTO> dailyStats = generateDailyStats(startDate, endDate);
        
        // Generate monthly stats
        List<DashboardStatisticsDTO.MonthlyStatsDTO> monthlyStats = generateMonthlyStats(startDate, endDate);
        
        // Category distributions
        Map<String, Long> booksByCategory = bookRepository.countBooksByCategory();
        Map<String, Long> ordersByStatus = orderRepository.countOrdersByStatus();
        Map<String, Long> loansByStatus = loanRepository.countLoansByStatus();
        Map<String, Long> usersByRole = getUsersByRoleFromAuthenService();
        
        // Performance metrics
        Map<String, Object> performanceMetrics = calculatePerformanceMetrics();
        
        // Top items
        List<DashboardStatisticsDTO.TopBookDTO> mostBorrowedBooks = getMostBorrowedBooks();
        List<DashboardStatisticsDTO.TopBookDTO> mostSoldBooks = getMostSoldBooks();
        List<DashboardStatisticsDTO.TopUserDTO> mostActiveUsers = getMostActiveUsers();
        
        return DashboardStatisticsDTO.builder()
            .dailyStats(dailyStats)
            .monthlyStats(monthlyStats)
            .booksByCategory(booksByCategory)
            .ordersByStatus(ordersByStatus)
            .loansByStatus(loansByStatus)
            .usersByRole(usersByRole)
            .performanceMetrics(performanceMetrics)
            .mostBorrowedBooks(mostBorrowedBooks)
            .mostSoldBooks(mostSoldBooks)
            .mostActiveUsers(mostActiveUsers)
            .build();
    }

    @Override
    public SystemConfigDTO getSystemConfiguration() {
        // TODO: Implement system configuration retrieval
        // For now, return default configuration
        return SystemConfigDTO.builder()
            .maxLoansPerUser(5)
            .loanDurationDays(14)
            .dailyFineAmount(new BigDecimal("5000"))
            .maxFineAmount(new BigDecimal("50000"))
            .gracePeriodDays(3)
            .shippingFee(new BigDecimal("25000"))
            .freeShippingThreshold(new BigDecimal("200000"))
            .taxRate(new BigDecimal("0.1"))
            .orderTimeoutMinutes(15)
            .maxFileSize(500L * 1024 * 1024) // 500MB
            .allowedFileTypes("pdf,doc,docx,txt,jpg,png")
            .documentsPerPage(20)
            .emailNotificationsEnabled(true)
            .smsNotificationsEnabled(false)
            .notificationRetentionDays(90)
            .dueSoonReminderDays(3)
            .systemName("Library Management System")
            .systemVersion("1.0.0")
            .adminEmail("admin@library.com")
            .supportEmail("support@library.com")
            .maintenanceMode(false)
            .maintenanceMessage("")
            .build();
    }

    @Override
    public SystemConfigDTO updateSystemConfiguration(SystemConfigDTO config) {
        // TODO: Implement system configuration update
        log.info("System configuration updated");
        return config;
    }

    @Override
    public DashboardStatisticsDTO.DailyStatsDTO getDailyStatistics(LocalDate date) {
        // Get user stats from AuthenService for specific date
        Long newUsers = 0L; // TODO: Implement
        
        Long newLoans = loanRepository.countByCreatedAtBetween(
            date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        
        Long newOrders = orderRepository.countByCreatedAtBetween(
            date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        
        BigDecimal revenue = orderRepository.getTotalRevenueByDate(date);
        
        Long documentDownloads = documentAccessLogRepository.countByAccessTypeAndAccessTimeBetween(
            "DOWNLOAD", date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        
        return DashboardStatisticsDTO.DailyStatsDTO.builder()
            .date(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
            .newUsers(newUsers)
            .newLoans(newLoans)
            .newOrders(newOrders)
            .revenue(revenue != null ? revenue : BigDecimal.ZERO)
            .documentDownloads(documentDownloads)
            .build();
    }

    @Override
    public DashboardStatisticsDTO.MonthlyStatsDTO getMonthlyStatistics(int year, int month) {
        // TODO: Implement monthly statistics
        return DashboardStatisticsDTO.MonthlyStatsDTO.builder()
            .month(String.format("%04d-%02d", year, month))
            .totalUsers(0L)
            .totalLoans(0L)
            .totalOrders(0L)
            .totalRevenue(BigDecimal.ZERO)
            .totalDocumentDownloads(0L)
            .build();
    }

    @Override
    @CacheEvict(value = {"dashboard-overview", "dashboard-statistics"}, allEntries = true)
    public void refreshDashboardCache() {
        log.info("Dashboard cache refreshed");
    }

    // Private helper methods

    private Map<String, Long> getUserStatisticsFromAuthenService() {
        try {
            // Call AuthenService to get user statistics
            String url = authenServiceUrl + "/api/v1/admin/users/statistics";
            // TODO: Implement actual REST call with proper authentication
            Map<String, Long> stats = new HashMap<>();
            stats.put("totalUsers", 100L);
            stats.put("activeUsers", 85L);
            stats.put("newUsersToday", 5L);
            stats.put("newUsersThisWeek", 20L);
            return stats;
        } catch (Exception e) {
            log.error("Error fetching user statistics from AuthenService", e);
            return new HashMap<>();
        }
    }

    private Map<String, Long> getUsersByRoleFromAuthenService() {
        // TODO: Implement
        Map<String, Long> usersByRole = new HashMap<>();
        usersByRole.put("USER", 80L);
        usersByRole.put("LIBRARIAN", 15L);
        usersByRole.put("ADMIN", 5L);
        return usersByRole;
    }

    private Long getDocumentCount() {
        // TODO: Call DocumentService to get document count
        return 150L;
    }

    private Long getDocumentsUploadedThisMonth() {
        // TODO: Call DocumentService to get documents uploaded this month
        return 25L;
    }

    private List<DashboardStatisticsDTO.DailyStatsDTO> generateDailyStats(LocalDate startDate, LocalDate endDate) {
        // TODO: Implement daily stats generation
        return List.of();
    }

    private List<DashboardStatisticsDTO.MonthlyStatsDTO> generateMonthlyStats(LocalDate startDate, LocalDate endDate) {
        // TODO: Implement monthly stats generation
        return List.of();
    }

    private Map<String, Object> calculatePerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("averageLoanDuration", 12.5);
        metrics.put("orderFulfillmentRate", 0.95);
        metrics.put("userSatisfactionScore", 4.2);
        metrics.put("systemUptime", 0.999);
        return metrics;
    }

    private List<DashboardStatisticsDTO.TopBookDTO> getMostBorrowedBooks() {
        // TODO: Implement
        return List.of();
    }

    private List<DashboardStatisticsDTO.TopBookDTO> getMostSoldBooks() {
        // TODO: Implement
        return List.of();
    }

    private List<DashboardStatisticsDTO.TopUserDTO> getMostActiveUsers() {
        // TODO: Implement
        return List.of();
    }
}