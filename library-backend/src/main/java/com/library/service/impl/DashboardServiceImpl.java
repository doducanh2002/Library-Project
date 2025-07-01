package com.library.service.impl;

import com.library.dto.DashboardOverviewDTO;
import com.library.dto.SystemStatsDTO;
import com.library.dto.DashboardActivityDTO;
import com.library.repository.*;
import com.library.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
    
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final OrderRepository orderRepository;
    private final DocumentRepository documentRepository;
    private final NotificationRepository notificationRepository;
    private final CategoryRepository categoryRepository;
    
    @Override
    @Cacheable(value = "dashboard-overview", unless = "#result == null")
    public DashboardOverviewDTO getDashboardOverview() {
        log.info("Generating dashboard overview");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfToday = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfWeek = now.minusDays(7);
        
        return DashboardOverviewDTO.builder()
                // User statistics
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countActiveUsers())
                .newUsersThisMonth(userRepository.countNewUsersSince(startOfMonth))
                .newUsersToday(userRepository.countNewUsersSince(startOfToday))
                
                // Book statistics
                .totalBooks(bookRepository.count())
                .availableBooks(bookRepository.countAvailableBooks())
                .borrowedBooks(bookRepository.countBorrowedBooks())
                .booksAddedThisMonth(bookRepository.countBooksAddedSince(startOfMonth))
                
                // Loan statistics
                .totalLoans(loanRepository.count())
                .activeLoans(loanRepository.countActiveLoans())
                .overdueLoans(loanRepository.countOverdueLoans())
                .loansThisMonth(loanRepository.countLoansSince(startOfMonth))
                .totalFinesCollected(loanRepository.sumTotalFinesCollected() != null ? 
                    loanRepository.sumTotalFinesCollected() : BigDecimal.ZERO)
                .pendingFines(loanRepository.sumPendingFines() != null ? 
                    loanRepository.sumPendingFines() : BigDecimal.ZERO)
                
                // Order statistics
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countPendingOrders())
                .completedOrders(orderRepository.countCompletedOrders())
                .ordersThisMonth(orderRepository.countOrdersSince(startOfMonth))
                .totalRevenue(orderRepository.sumTotalRevenue() != null ? 
                    orderRepository.sumTotalRevenue() : BigDecimal.ZERO)
                .revenueThisMonth(orderRepository.sumRevenueSince(startOfMonth) != null ? 
                    orderRepository.sumRevenueSince(startOfMonth) : BigDecimal.ZERO)
                
                // Document statistics
                .totalDocuments(documentRepository.countActiveDocuments())
                .publicDocuments(documentRepository.countPublicDocuments())
                .documentsUploadedThisMonth(documentRepository.countDocumentsUploadedSince(startOfMonth))
                .totalDownloads(documentRepository.sumDownloadCounts() != null ? 
                    documentRepository.sumDownloadCounts() : 0L)
                
                // Notification statistics
                .totalNotifications(notificationRepository.count())
                .unreadNotifications(notificationRepository.countUnreadNotifications())
                .notificationsThisWeek(notificationRepository.countNotificationsSince(startOfWeek))
                
                // System statistics
                .systemHealthy(checkSystemHealth())
                .totalStorageUsed(calculateStorageUsed())
                .storageUsedFormatted(formatFileSize(calculateStorageUsed()))
                
                // Recent activity
                .recentActivities(getRecentActivities(10))
                
                // Charts data
                .userRegistrationTrend(getUserRegistrationTrend(7))
                .loanTrend(getLoanTrend(7))
                .revenueTrend(getRevenueTrendMap(7))
                .bookCategoryDistribution(getBookCategoryDistribution())
                .orderStatusDistribution(getOrderStatusDistribution())
                .loanStatusDistribution(getLoanStatusDistribution())
                
                .generatedAt(now)
                .build();
    }
    
    @Override
    @Cacheable(value = "system-stats", unless = "#result == null")
    public SystemStatsDTO getSystemStatistics() {
        log.info("Generating system statistics");
        
        LocalDateTime now = LocalDateTime.now();
        Runtime runtime = Runtime.getRuntime();
        
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return SystemStatsDTO.builder()
                .systemVersion("1.0.0")
                .systemStartTime(getSystemStartTime())
                .systemUptime(getSystemUptime())
                .systemUptimeFormatted(formatUptime(getSystemUptime()))
                
                // Performance metrics
                .memoryUsage((double) usedMemory / totalMemory * 100)
                .totalMemory(totalMemory)
                .usedMemory(usedMemory)
                .memoryUsageFormatted(formatFileSize(usedMemory) + " / " + formatFileSize(totalMemory))
                
                // Database metrics
                .databaseResponseTime(measureDatabaseResponseTime())
                
                // Storage metrics
                .totalStorageSpace(getTotalStorageSpace())
                .usedStorageSpace(calculateStorageUsed())
                .freeStorageSpace(getTotalStorageSpace() - calculateStorageUsed())
                .storageUsagePercentage((double) calculateStorageUsed() / getTotalStorageSpace() * 100)
                .totalStorageFormatted(formatFileSize(getTotalStorageSpace()))
                .usedStorageFormatted(formatFileSize(calculateStorageUsed()))
                .freeStorageFormatted(formatFileSize(getTotalStorageSpace() - calculateStorageUsed()))
                
                // Application metrics
                .totalRequests(getTotalRequests())
                .successfulRequests(getSuccessfulRequests())
                .failedRequests(getFailedRequests())
                .successRate(getSuccessRate())
                .errorRate(getErrorRate())
                .averageResponseTime(getAverageResponseTime())
                
                // External services health
                .minioHealthy(checkMinioHealth())
                .redisHealthy(checkRedisHealth())
                
                // Alerts and warnings
                .alerts(generateAlerts())
                .warnings(generateWarnings())
                
                .generatedAt(now)
                .build();
    }
    
    @Override
    public List<DashboardActivityDTO> getRecentActivities(int limit) {
        List<DashboardActivityDTO> activities = new ArrayList<>();
        
        // Get recent user registrations
        userRepository.findRecentUsers(PageRequest.of(0, 5))
                .forEach(user -> activities.add(DashboardActivityDTO.builder()
                        .type("USER_REGISTRATION")
                        .title("New User Registration")
                        .description("User " + user.getUsername() + " registered")
                        .userId(user.getId().toString())
                        .userName(user.getUsername())
                        .timestamp(user.getCreatedAt())
                        .icon("user-plus")
                        .color("green")
                        .build()));
        
        // Get recent loans
        loanRepository.findRecentLoans(PageRequest.of(0, 5))
                .forEach(loan -> activities.add(DashboardActivityDTO.builder()
                        .type("BOOK_BORROWED")
                        .title("Book Borrowed")
                        .description("Book \"" + loan.getBook().getTitle() + "\" borrowed")
                        .userId(loan.getUser().getId().toString())
                        .userName(loan.getUser().getUsername())
                        .entityId(loan.getBook().getId().toString())
                        .entityName(loan.getBook().getTitle())
                        .timestamp(loan.getCreatedAt())
                        .icon("book")
                        .color("blue")
                        .build()));
        
        // Get recent orders
        orderRepository.findRecentOrders(PageRequest.of(0, 5))
                .forEach(order -> activities.add(DashboardActivityDTO.builder()
                        .type("ORDER_PLACED")
                        .title("Order Placed")
                        .description("Order #" + order.getOrderCode() + " placed")
                        .userId(order.getUser().getId().toString())
                        .userName(order.getUser().getUsername())
                        .entityId(order.getId().toString())
                        .entityName(order.getOrderCode())
                        .timestamp(order.getCreatedAt())
                        .icon("shopping-cart")
                        .color("orange")
                        .build()));
        
        // Sort by timestamp and limit
        return activities.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Long> getUserRegistrationTrend(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        Map<String, Long> trend = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 0; i < days; i++) {
            LocalDateTime date = startDate.plusDays(i);
            LocalDateTime nextDate = date.plusDays(1);
            String dateKey = date.format(formatter);
            
            Long count = userRepository.countUserRegistrationsBetween(date, nextDate);
            trend.put(dateKey, count != null ? count : 0L);
        }
        
        return trend;
    }
    
    @Override
    public Map<String, Long> getLoanTrend(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        Map<String, Long> trend = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 0; i < days; i++) {
            LocalDateTime date = startDate.plusDays(i);
            LocalDateTime nextDate = date.plusDays(1);
            String dateKey = date.format(formatter);
            
            Long count = loanRepository.countLoansBetween(date, nextDate);
            trend.put(dateKey, count != null ? count : 0L);
        }
        
        return trend;
    }
    
    @Override
    public Map<String, Object> getRevenueTrend(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        Map<String, BigDecimal> dailyRevenue = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 0; i < days; i++) {
            LocalDateTime date = startDate.plusDays(i);
            LocalDateTime nextDate = date.plusDays(1);
            String dateKey = date.format(formatter);
            
            BigDecimal revenue = orderRepository.sumRevenueBetween(date, nextDate);
            dailyRevenue.put(dateKey, revenue != null ? revenue : BigDecimal.ZERO);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("daily", dailyRevenue);
        result.put("total", dailyRevenue.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        return result;
    }
    
    private Map<String, BigDecimal> getRevenueTrendMap(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        Map<String, BigDecimal> trend = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 0; i < days; i++) {
            LocalDateTime date = startDate.plusDays(i);
            LocalDateTime nextDate = date.plusDays(1);
            String dateKey = date.format(formatter);
            
            BigDecimal revenue = orderRepository.sumRevenueBetween(date, nextDate);
            trend.put(dateKey, revenue != null ? revenue : BigDecimal.ZERO);
        }
        
        return trend;
    }
    
    @Override
    public Map<String, Long> getBookCategoryDistribution() {
        List<Object[]> results = categoryRepository.findCategoryBookCounts();
        Map<String, Long> distribution = new LinkedHashMap<>();
        
        for (Object[] result : results) {
            String categoryName = (String) result[0];
            Long bookCount = (Long) result[1];
            distribution.put(categoryName, bookCount);
        }
        
        return distribution;
    }
    
    @Override
    public Map<String, Long> getOrderStatusDistribution() {
        List<Object[]> results = orderRepository.findOrderStatusCounts();
        Map<String, Long> distribution = new LinkedHashMap<>();
        
        for (Object[] result : results) {
            String status = result[0].toString();
            Long count = (Long) result[1];
            distribution.put(status, count);
        }
        
        return distribution;
    }
    
    @Override
    public Map<String, Long> getLoanStatusDistribution() {
        List<Object[]> results = loanRepository.findLoanStatusCounts();
        Map<String, Long> distribution = new LinkedHashMap<>();
        
        for (Object[] result : results) {
            String status = result[0].toString();
            Long count = (Long) result[1];
            distribution.put(status, count);
        }
        
        return distribution;
    }
    
    @Override
    public List<Map<String, Object>> getTopPerformingBooks(int limit) {
        return bookRepository.findTopPerformingBooks(PageRequest.of(0, limit))
                .stream()
                .map(book -> {
                    Map<String, Object> bookData = new HashMap<>();
                    bookData.put("id", book.getId());
                    bookData.put("title", book.getTitle());
                    bookData.put("author", book.getAuthors().stream()
                            .map(author -> author.getName())
                            .collect(Collectors.joining(", ")));
                    bookData.put("borrowCount", book.getTotalBorrows());
                    bookData.put("purchaseCount", book.getTotalPurchases());
                    return bookData;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Map<String, Object>> getMostActiveUsers(int limit) {
        return userRepository.findMostActiveUsers(PageRequest.of(0, limit))
                .stream()
                .map(user -> {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", user.getId());
                    userData.put("username", user.getUsername());
                    userData.put("email", user.getEmail());
                    userData.put("loanCount", loanRepository.countByUserId(user.getId()));
                    userData.put("orderCount", orderRepository.countByUserId(user.getId()));
                    return userData;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getSystemAlerts() {
        Map<String, Object> alerts = new HashMap<>();
        
        // Check for overdue loans
        Long overdueCount = loanRepository.countOverdueLoans();
        if (overdueCount > 0) {
            alerts.put("overdue_loans", Map.of(
                    "level", "warning",
                    "message", overdueCount + " loans are overdue",
                    "count", overdueCount
            ));
        }
        
        // Check for low stock books
        Long lowStockCount = bookRepository.countLowStockBooks(5);
        if (lowStockCount > 0) {
            alerts.put("low_stock", Map.of(
                    "level", "warning",
                    "message", lowStockCount + " books have low stock",
                    "count", lowStockCount
            ));
        }
        
        // Check for pending orders
        Long pendingOrderCount = orderRepository.countPendingOrders();
        if (pendingOrderCount > 10) {
            alerts.put("pending_orders", Map.of(
                    "level", "info",
                    "message", pendingOrderCount + " orders pending processing",
                    "count", pendingOrderCount
            ));
        }
        
        return alerts;
    }
    
    @Override
    public Map<String, Object> getPerformanceMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> metrics = new HashMap<>();
        
        // API metrics would be collected from monitoring system
        metrics.put("totalRequests", getTotalRequests());
        metrics.put("averageResponseTime", getAverageResponseTime());
        metrics.put("errorRate", getErrorRate());
        
        return metrics;
    }
    
    @Override
    public Map<String, Object> getFinancialSummary(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> summary = new HashMap<>();
        
        BigDecimal totalRevenue = orderRepository.sumRevenueBetween(startDate, endDate);
        BigDecimal totalFines = loanRepository.sumFinesBetween(startDate, endDate);
        
        summary.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        summary.put("totalFines", totalFines != null ? totalFines : BigDecimal.ZERO);
        summary.put("totalIncome", 
                (totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                        .add(totalFines != null ? totalFines : BigDecimal.ZERO));
        
        return summary;
    }
    
    @Override
    public Boolean checkSystemHealth() {
        try {
            // Check database connectivity
            userRepository.count();
            
            // Check external services
            boolean minioHealthy = checkMinioHealth();
            boolean redisHealthy = checkRedisHealth();
            
            return minioHealthy && redisHealthy;
        } catch (Exception e) {
            log.error("System health check failed", e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // These would be implemented with actual cache metrics
        stats.put("hitRate", 85.5);
        stats.put("missRate", 14.5);
        stats.put("evictionCount", 0L);
        stats.put("size", 1024L);
        
        return stats;
    }
    
    // Helper methods
    
    private Long calculateStorageUsed() {
        Long documentStorage = documentRepository.sumFileSizes();
        return documentStorage != null ? documentStorage : 0L;
    }
    
    private Long getTotalStorageSpace() {
        // This would be configured based on available storage
        return 1024L * 1024L * 1024L * 100L; // 100 GB
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    private LocalDateTime getSystemStartTime() {
        // This would be tracked from application startup
        return LocalDateTime.now().minusHours(24);
    }
    
    private Long getSystemUptime() {
        return ChronoUnit.SECONDS.between(getSystemStartTime(), LocalDateTime.now());
    }
    
    private String formatUptime(Long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = ((seconds % 86400) % 3600) / 60;
        return String.format("%dd %dh %dm", days, hours, minutes);
    }
    
    private Double measureDatabaseResponseTime() {
        long start = System.currentTimeMillis();
        try {
            userRepository.count();
            return (double) (System.currentTimeMillis() - start);
        } catch (Exception e) {
            return -1.0;
        }
    }
    
    private Boolean checkMinioHealth() {
        // This would ping MinIO service
        return true;
    }
    
    private Boolean checkRedisHealth() {
        // This would ping Redis service
        return true;
    }
    
    private Long getTotalRequests() {
        // This would be tracked by monitoring
        return 50000L;
    }
    
    private Long getSuccessfulRequests() {
        return 48500L;
    }
    
    private Long getFailedRequests() {
        return 1500L;
    }
    
    private Double getSuccessRate() {
        return 97.0;
    }
    
    private Double getErrorRate() {
        return 3.0;
    }
    
    private Double getAverageResponseTime() {
        return 120.5;
    }
    
    private Map<String, Object> generateAlerts() {
        Map<String, Object> alerts = new HashMap<>();
        
        // Critical alerts
        if (getErrorRate() > 5.0) {
            alerts.put("high_error_rate", Map.of(
                    "level", "critical",
                    "message", "Error rate is above 5%"
            ));
        }
        
        return alerts;
    }
    
    private Map<String, Object> generateWarnings() {
        Map<String, Object> warnings = new HashMap<>();
        
        // Memory usage warning
        Runtime runtime = Runtime.getRuntime();
        double memoryUsage = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.totalMemory() * 100;
        if (memoryUsage > 80) {
            warnings.put("high_memory_usage", Map.of(
                    "level", "warning",
                    "message", "Memory usage is above 80%",
                    "value", memoryUsage
            ));
        }
        
        return warnings;
    }
}