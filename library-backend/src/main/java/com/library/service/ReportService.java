package com.library.service;

import com.library.client.AuthServiceClient;
import com.library.dto.response.*;
import com.library.entity.enums.LoanStatus;
import com.library.entity.enums.OrderStatus;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final OrderRepository orderRepository;
    private final AuthServiceClient authServiceClient;
    private final DocumentRepository documentRepository;
    private final CategoryRepository categoryRepository;

    public DashboardStatsDTO getDashboardStats() {
        log.info("Generating dashboard statistics");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime monthStart = YearMonth.from(now).atDay(1).atStartOfDay();

        return DashboardStatsDTO.builder()
                .totalBooks(bookRepository.count())
                .totalActiveLoans(loanRepository.countByStatus(LoanStatus.BORROWED))
                .totalOverdueLoans(loanRepository.countOverdueLoans())
                .totalUsers(authServiceClient.getTotalUsersCount())
                .totalOrders(orderRepository.count())
                .totalRevenue(orderRepository.calculateTotalRevenue())
                .totalDocuments(documentRepository.count())
                
                .todayLoans(loanRepository.countLoansByDateRange(todayStart, now))
                .todayReturns(loanRepository.countReturnsByDateRange(todayStart, now))
                .todayNewUsers(authServiceClient.getUsersCountByDateRange(todayStart, now))
                .todayOrders(orderRepository.countByCreatedAtBetween(todayStart, now))
                .todayRevenue(orderRepository.calculateRevenueByDateRange(todayStart, now))
                
                .monthlyLoans(loanRepository.countLoansByDateRange(monthStart, now))
                .monthlyReturns(loanRepository.countReturnsByDateRange(monthStart, now))
                .monthlyNewUsers(authServiceClient.getUsersCountByDateRange(monthStart, now))
                .monthlyOrders(orderRepository.countByCreatedAtBetween(monthStart, now))
                .monthlyRevenue(orderRepository.calculateRevenueByDateRange(monthStart, now))
                
                .popularBooks(getPopularBooks())
                .popularCategories(getPopularCategories())
                .recentActivities(getRecentActivities())
                .systemHealth(getSystemHealth())
                .lastUpdated(now)
                .build();
    }

    public ReportDataDTO generateLoanReport(LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("Generating loan report from {} to {}", fromDate, toDate);
        
        List<Object[]> loanData = loanRepository.getLoanReportData(fromDate, toDate);
        
        List<String> labels = new ArrayList<>();
        List<Long> borrowedCounts = new ArrayList<>();
        List<Long> returnedCounts = new ArrayList<>();
        
        // Process data for chart
        Map<String, Map<String, Long>> dailyStats = new HashMap<>();
        loanData.forEach(row -> {
            String date = row[0].toString();
            String status = row[1].toString();
            Long count = ((Number) row[2]).longValue();
            
            dailyStats.computeIfAbsent(date, k -> new HashMap<>()).put(status, count);
        });
        
        // Create chart data
        for (Map.Entry<String, Map<String, Long>> entry : dailyStats.entrySet()) {
            labels.add(entry.getKey());
            borrowedCounts.add(entry.getValue().getOrDefault("BORROWED", 0L));
            returnedCounts.add(entry.getValue().getOrDefault("RETURNED", 0L));
        }
        
        List<Object> datasets = Arrays.asList(
            Map.of("label", "Sách mượn", "data", borrowedCounts, "backgroundColor", "#3B82F6"),
            Map.of("label", "Sách trả", "data", returnedCounts, "backgroundColor", "#10B981")
        );
        
        // Table data
        List<String> tableHeaders = Arrays.asList("Ngày", "Tổng mượn", "Tổng trả", "Quá hạn", "Tỷ lệ trả đúng hạn");
        List<List<Object>> tableData = generateLoanTableData(fromDate, toDate);
        
        // Summary
        Map<String, Object> summary = Map.of(
            "totalLoans", loanRepository.countLoansByDateRange(fromDate, toDate),
            "totalReturns", loanRepository.countReturnsByDateRange(fromDate, toDate),
            "overdueLoans", loanRepository.countOverdueLoansByDateRange(fromDate, toDate),
            "onTimeReturnRate", calculateOnTimeReturnRate(fromDate, toDate)
        );
        
        return ReportDataDTO.builder()
                .reportType("LOAN_REPORT")
                .title("Báo cáo mượn trả sách")
                .description("Thống kê tình hình mượn trả sách từ " + fromDate.toLocalDate() + " đến " + toDate.toLocalDate())
                .generatedAt(LocalDateTime.now())
                .fromDate(fromDate)
                .toDate(toDate)
                .labels(labels)
                .datasets(datasets)
                .tableHeaders(tableHeaders)
                .tableData(tableData)
                .summary(summary)
                .availableFormats(Arrays.asList("PDF", "EXCEL", "CSV"))
                .build();
    }

    public ReportDataDTO generateOrderReport(LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("Generating order report from {} to {}", fromDate, toDate);
        
        List<Object[]> orderData = orderRepository.getOrderReportData(fromDate, toDate);
        
        List<String> labels = new ArrayList<>();
        List<BigDecimal> revenueData = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();
        
        // Process data
        Map<String, Map<String, Object>> dailyStats = new HashMap<>();
        orderData.forEach(row -> {
            String date = row[0].toString();
            BigDecimal revenue = (BigDecimal) row[1];
            Long count = ((Number) row[2]).longValue();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("revenue", revenue);
            stats.put("count", count);
            dailyStats.put(date, stats);
        });
        
        // Create chart data
        for (Map.Entry<String, Map<String, Object>> entry : dailyStats.entrySet()) {
            labels.add(entry.getKey());
            revenueData.add((BigDecimal) entry.getValue().get("revenue"));
            orderCounts.add((Long) entry.getValue().get("count"));
        }
        
        List<Object> datasets = Arrays.asList(
            Map.of("label", "Doanh thu", "data", revenueData, "backgroundColor", "#F59E0B", "yAxisID", "y"),
            Map.of("label", "Số đơn hàng", "data", orderCounts, "backgroundColor", "#EF4444", "yAxisID", "y1")
        );
        
        // Table data
        List<String> tableHeaders = Arrays.asList("Ngày", "Số đơn hàng", "Doanh thu", "Đơn hàng trung bình", "Trạng thái phổ biến");
        List<List<Object>> tableData = generateOrderTableData(fromDate, toDate);
        
        // Summary
        Map<String, Object> summary = Map.of(
            "totalOrders", orderRepository.countByCreatedAtBetween(fromDate, toDate),
            "totalRevenue", orderRepository.calculateRevenueByDateRange(fromDate, toDate),
            "averageOrderValue", calculateAverageOrderValue(fromDate, toDate),
            "completedOrdersRate", calculateCompletedOrdersRate(fromDate, toDate)
        );
        
        return ReportDataDTO.builder()
                .reportType("ORDER_REPORT")
                .title("Báo cáo đơn hàng")
                .description("Thống kê đơn hàng và doanh thu từ " + fromDate.toLocalDate() + " đến " + toDate.toLocalDate())
                .generatedAt(LocalDateTime.now())
                .fromDate(fromDate)
                .toDate(toDate)
                .labels(labels)
                .datasets(datasets)
                .tableHeaders(tableHeaders)
                .tableData(tableData)
                .summary(summary)
                .availableFormats(Arrays.asList("PDF", "EXCEL", "CSV"))
                .build();
    }

    private List<PopularBookDTO> getPopularBooks() {
        List<Object[]> results = bookRepository.findMostPopularBooks(10);
        return results.stream().map(row -> PopularBookDTO.builder()
                .bookId(((Number) row[0]).longValue())
                .title((String) row[1])
                .isbn((String) row[2])
                .authorName((String) row[3])
                .totalLoans(((Number) row[4]).longValue())
                .totalOrders(((Number) row[5]).longValue())
                .avgRating(row[6] != null ? ((Number) row[6]).doubleValue() : 0.0)
                .coverImageUrl((String) row[7])
                .build()).collect(Collectors.toList());
    }

    private List<PopularCategoryDTO> getPopularCategories() {
        List<Object[]> results = categoryRepository.findMostPopularCategories(5);
        return results.stream().map(row -> PopularCategoryDTO.builder()
                .categoryId(((Number) row[0]).longValue())
                .categoryName((String) row[1])
                .totalBooks(((Number) row[2]).longValue())
                .totalLoans(((Number) row[3]).longValue())
                .totalOrders(((Number) row[4]).longValue())
                .percentage(((Number) row[5]).doubleValue())
                .build()).collect(Collectors.toList());
    }

    private List<RecentActivityDTO> getRecentActivities() {
        List<RecentActivityDTO> activities = new ArrayList<>();
        
        // Get recent loans
        loanRepository.findTop5ByOrderByCreatedAtDesc().forEach(loan -> {
            activities.add(RecentActivityDTO.builder()
                    .activityType("LOAN_CREATED")
                    .description("Tạo yêu cầu mượn sách")
                    .userName(loan.getUser().getFullName())
                    .itemName(loan.getBook().getTitle())
                    .timestamp(loan.getCreatedAt())
                    .status(loan.getStatus().name())
                    .icon("book")
                    .build());
        });
        
        // Get recent orders
        orderRepository.findTop5ByOrderByCreatedAtDesc().forEach(order -> {
            activities.add(RecentActivityDTO.builder()
                    .activityType("ORDER_PLACED")
                    .description("Đặt hàng mới")
                    .userName(order.getUser().getFullName())
                    .itemName("Đơn hàng #" + order.getId())
                    .timestamp(order.getCreatedAt())
                    .status(order.getStatus().name())
                    .icon("shopping-cart")
                    .build());
        });
        
        // Sort by timestamp descending
        return activities.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private SystemHealthDTO getSystemHealth() {
        return SystemHealthDTO.builder()
                .databaseStatus("HEALTHY")
                .redisStatus("HEALTHY")
                .minioStatus("HEALTHY")
                .authServiceStatus("HEALTHY")
                .cpuUsage(65.0)
                .memoryUsage(72.5)
                .diskUsage(45000000000L) // 45GB
                .diskTotal(100000000000L) // 100GB
                .overallStatus("HEALTHY")
                .build();
    }

    private List<List<Object>> generateLoanTableData(LocalDateTime fromDate, LocalDateTime toDate) {
        // Implementation for loan table data
        return new ArrayList<>();
    }

    private List<List<Object>> generateOrderTableData(LocalDateTime fromDate, LocalDateTime toDate) {
        // Implementation for order table data
        return new ArrayList<>();
    }

    private Double calculateOnTimeReturnRate(LocalDateTime fromDate, LocalDateTime toDate) {
        Long totalReturns = loanRepository.countReturnsByDateRange(fromDate, toDate);
        Long onTimeReturns = loanRepository.countOnTimeReturnsByDateRange(fromDate, toDate);
        return totalReturns > 0 ? (onTimeReturns.doubleValue() / totalReturns.doubleValue()) * 100 : 0.0;
    }

    private BigDecimal calculateAverageOrderValue(LocalDateTime fromDate, LocalDateTime toDate) {
        BigDecimal totalRevenue = orderRepository.calculateRevenueByDateRange(fromDate, toDate);
        Long totalOrders = orderRepository.countByCreatedAtBetween(fromDate, toDate);
        return totalOrders > 0 ? totalRevenue.divide(new BigDecimal(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
    }

    private Double calculateCompletedOrdersRate(LocalDateTime fromDate, LocalDateTime toDate) {
        Long totalOrders = orderRepository.countByCreatedAtBetween(fromDate, toDate);
        Long completedOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.COMPLETED, fromDate, toDate);
        return totalOrders > 0 ? (completedOrders.doubleValue() / totalOrders.doubleValue()) * 100 : 0.0;
    }
}