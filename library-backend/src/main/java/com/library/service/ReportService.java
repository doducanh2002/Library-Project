package com.library.service;

import com.library.dto.admin.ReportDTO;
import com.library.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {
    
    private final DashboardRepository dashboardRepository;
    
    public ReportDTO.ReportResponse generateReport(ReportDTO.ReportRequest request) {
        log.info("Generating report of type: {}", request.getReportType());
        
        try {
            String reportId = UUID.randomUUID().toString();
            String fileName = generateFileName(request);
            
            // Simulate report generation (in real implementation, this would be async)
            Object reportData = generateReportData(request);
            
            // In real implementation, you would:
            // 1. Generate the actual file (PDF/Excel/CSV)
            // 2. Save it to storage (MinIO, local file system, etc.)
            // 3. Return download URL
            
            return ReportDTO.ReportResponse.builder()
                    .reportId(reportId)
                    .reportTitle(request.getReportTitle() != null ? request.getReportTitle() : getDefaultReportTitle(request.getReportType()))
                    .reportType(request.getReportType())
                    .exportFormat(request.getExportFormat() != null ? request.getExportFormat() : ReportDTO.ReportRequest.ExportFormat.PDF)
                    .fileName(fileName)
                    .downloadUrl("/api/v1/admin/reports/download/" + reportId)
                    .status(ReportDTO.ReportResponse.ReportStatus.COMPLETED)
                    .generatedAt(LocalDateTime.now())
                    .generatedBy("Admin") // In real implementation, get from SecurityContext
                    .fileSizeBytes(calculateEstimatedFileSize(request))
                    .build();
            
        } catch (Exception e) {
            log.error("Failed to generate report", e);
            throw new RuntimeException("Report generation failed: " + e.getMessage());
        }
    }
    
    public ReportDTO.LoanReportData generateLoanReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating loan report from {} to {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(12);
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
        
        // Get basic loan statistics
        Long totalLoans = dashboardRepository.countTotalLoans();
        Long activeLoans = dashboardRepository.countActiveLoans();
        Long overdueLoans = dashboardRepository.countOverdueLoans();
        Double averageLoanDuration = dashboardRepository.getAverageLoanDuration();
        BigDecimal totalFinesCollected = dashboardRepository.getTotalFinesCollected();
        
        // Calculate returned loans
        Long returnedLoans = totalLoans - activeLoans;
        
        // Get loans by category
        List<ReportDTO.LoanReportData.LoanSummaryItem> loansByCategory = 
                dashboardRepository.getBooksBorrowedByCategory().stream()
                        .map(row -> ReportDTO.LoanReportData.LoanSummaryItem.builder()
                                .label((String) row[0])
                                .count(((Number) row[1]).longValue())
                                .percentage(calculatePercentage(((Number) row[1]).longValue(), totalLoans))
                                .build())
                        .collect(Collectors.toList());
        
        // Get loans by month
        List<ReportDTO.LoanReportData.LoanSummaryItem> loansByMonth = 
                dashboardRepository.getLoansByMonth(startDateTime).stream()
                        .map(row -> ReportDTO.LoanReportData.LoanSummaryItem.builder()
                                .label(String.format("%d-%02d", (Integer) row[0], (Integer) row[1]))
                                .count(((Number) row[2]).longValue())
                                .percentage(calculatePercentage(((Number) row[2]).longValue(), totalLoans))
                                .build())
                        .collect(Collectors.toList());
        
        // Get most borrowed books
        List<ReportDTO.LoanReportData.PopularBookItem> mostBorrowedBooks = 
                dashboardRepository.getPopularBooks().stream()
                        .limit(10)
                        .map(row -> ReportDTO.LoanReportData.PopularBookItem.builder()
                                .bookTitle((String) row[1])
                                .author((String) row[2])
                                .category("Unknown") // Would need additional query to get category
                                .borrowCount((Long) row[3])
                                .rating(0.0) // Would need rating system
                                .build())
                        .collect(Collectors.toList());
        
        // Get top borrowers
        List<ReportDTO.LoanReportData.UserLoanSummary> topBorrowers = 
                dashboardRepository.getTopBorrowers().stream()
                        .limit(10)
                        .map(row -> ReportDTO.LoanReportData.UserLoanSummary.builder()
                                .userName((String) row[1])
                                .email((String) row[2])
                                .totalLoans(((Number) row[3]).longValue())
                                .currentLoans(((Number) row[4]).longValue())
                                .totalFines((BigDecimal) row[5])
                                .build())
                        .collect(Collectors.toList());
        
        return ReportDTO.LoanReportData.builder()
                .totalLoans(totalLoans)
                .activeLoans(activeLoans)
                .returnedLoans(returnedLoans)
                .overdueLoans(overdueLoans)
                .averageLoanDuration(averageLoanDuration)
                .totalFinesCollected(totalFinesCollected)
                .loansByCategory(loansByCategory)
                .loansByMonth(loansByMonth)
                .mostBorrowedBooks(mostBorrowedBooks)
                .topBorrowers(topBorrowers)
                .build();
    }
    
    public ReportDTO.SalesReportData generateSalesReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating sales report from {} to {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(12);
        
        // Get basic sales statistics
        BigDecimal totalRevenue = dashboardRepository.getTotalRevenue();
        Long totalOrders = dashboardRepository.countTotalOrders();
        BigDecimal averageOrderValue = dashboardRepository.getAverageOrderValue();
        
        // Estimated values (in real implementation, you'd have proper queries)
        BigDecimal totalProfit = totalRevenue.multiply(BigDecimal.valueOf(0.3)); // Assuming 30% profit margin
        Long totalItemsSold = totalOrders * 2; // Estimated 2 items per order
        
        // Get sales by category
        List<ReportDTO.SalesReportData.SalesSummaryItem> salesByCategory = 
                dashboardRepository.getBooksSoldByCategory().stream()
                        .map(row -> {
                            Long quantity = ((Number) row[1]).longValue();
                            BigDecimal revenue = BigDecimal.valueOf(quantity * 50); // Estimated $50 per book
                            return ReportDTO.SalesReportData.SalesSummaryItem.builder()
                                    .label((String) row[0])
                                    .revenue(revenue)
                                    .quantity(quantity)
                                    .percentage(calculatePercentage(revenue, totalRevenue))
                                    .build();
                        })
                        .collect(Collectors.toList());
        
        // Get sales by month
        List<ReportDTO.SalesReportData.SalesSummaryItem> salesByMonth = 
                dashboardRepository.getRevenueByMonth(startDateTime).entrySet().stream()
                        .map(entry -> ReportDTO.SalesReportData.SalesSummaryItem.builder()
                                .label(entry.getKey())
                                .revenue(entry.getValue())
                                .quantity(entry.getValue().divide(averageOrderValue, RoundingMode.HALF_UP).longValue())
                                .percentage(calculatePercentage(entry.getValue(), totalRevenue))
                                .build())
                        .collect(Collectors.toList());
        
        // Get top selling books (mock data)
        List<ReportDTO.SalesReportData.TopSellingBookItem> topSellingBooks = Arrays.asList(
                ReportDTO.SalesReportData.TopSellingBookItem.builder()
                        .bookTitle("Spring Boot in Action")
                        .author("Craig Walls")
                        .category("Programming")
                        .quantitySold(150L)
                        .revenue(BigDecimal.valueOf(7500))
                        .avgPrice(BigDecimal.valueOf(50))
                        .build()
        );
        
        // Get top customers
        List<ReportDTO.SalesReportData.CustomerSummary> topCustomers = 
                dashboardRepository.getTopCustomers().stream()
                        .limit(10)
                        .map(row -> ReportDTO.SalesReportData.CustomerSummary.builder()
                                .customerName((String) row[1])
                                .email((String) row[2])
                                .totalSpent((BigDecimal) row[3])
                                .totalOrders(((Number) row[4]).longValue())
                                .lastOrderDate(row[5] != null ? ((java.sql.Date) row[5]).toLocalDate() : null)
                                .build())
                        .collect(Collectors.toList());
        
        return ReportDTO.SalesReportData.builder()
                .totalRevenue(totalRevenue)
                .totalProfit(totalProfit)
                .totalOrders(totalOrders)
                .totalItemsSold(totalItemsSold)
                .averageOrderValue(averageOrderValue)
                .salesByCategory(salesByCategory)
                .salesByMonth(salesByMonth)
                .topSellingBooks(topSellingBooks)
                .topCustomers(topCustomers)
                .build();
    }
    
    public ReportDTO.UserActivityReportData generateUserActivityReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating user activity report from {} to {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(12);
        
        // Get basic user statistics
        Long totalActiveUsers = dashboardRepository.countActiveUsers();
        Long newUsersCount = dashboardRepository.countNewUsers(startDateTime);
        Long returningUsersCount = totalActiveUsers - newUsersCount;
        Double userRetentionRate = totalActiveUsers > 0 ? 
                (returningUsersCount.doubleValue() / totalActiveUsers.doubleValue()) * 100 : 0.0;
        
        // Get users by registration month
        List<ReportDTO.UserActivityReportData.UserActivityItem> usersByRegistrationMonth = 
                dashboardRepository.getUserRegistrationsByMonth(startDateTime).entrySet().stream()
                        .map(entry -> ReportDTO.UserActivityReportData.UserActivityItem.builder()
                                .label(entry.getKey())
                                .count(entry.getValue())
                                .percentage(calculatePercentage(entry.getValue(), totalActiveUsers))
                                .build())
                        .collect(Collectors.toList());
        
        // Mock data for user activity levels
        List<ReportDTO.UserActivityReportData.UserActivityItem> usersByActivity = Arrays.asList(
                ReportDTO.UserActivityReportData.UserActivityItem.builder()
                        .label("High Activity")
                        .count(totalActiveUsers / 5)
                        .percentage(20.0)
                        .build(),
                ReportDTO.UserActivityReportData.UserActivityItem.builder()
                        .label("Medium Activity")
                        .count(totalActiveUsers * 3 / 5)
                        .percentage(60.0)
                        .build(),
                ReportDTO.UserActivityReportData.UserActivityItem.builder()
                        .label("Low Activity")
                        .count(totalActiveUsers / 5)
                        .percentage(20.0)
                        .build()
        );
        
        // Get top active users (combining borrowers and customers)
        List<ReportDTO.UserActivityReportData.UserEngagementSummary> topActiveUsers = 
                dashboardRepository.getTopBorrowers().stream()
                        .limit(10)
                        .map(row -> ReportDTO.UserActivityReportData.UserEngagementSummary.builder()
                                .userName((String) row[1])
                                .email((String) row[2])
                                .registrationDate(LocalDate.now().minusMonths(6)) // Mock data
                                .lastActivity(LocalDateTime.now().minusDays(1)) // Mock data
                                .totalLoans(((Number) row[3]).longValue())
                                .totalOrders(0L) // Would need cross-reference
                                .totalSpent(BigDecimal.ZERO) // Would need cross-reference
                                .build())
                        .collect(Collectors.toList());
        
        return ReportDTO.UserActivityReportData.builder()
                .totalActiveUsers(totalActiveUsers)
                .newUsersCount(newUsersCount)
                .returningUsersCount(returningUsersCount)
                .userRetentionRate(userRetentionRate)
                .usersByRegistrationMonth(usersByRegistrationMonth)
                .usersByActivity(usersByActivity)
                .topActiveUsers(topActiveUsers)
                .build();
    }
    
    public ReportDTO.OverdueReportData generateOverdueReport() {
        log.info("Generating overdue report");
        
        // Get overdue statistics
        Long totalOverdueItems = dashboardRepository.countOverdueLoans();
        BigDecimal totalOutstandingFines = dashboardRepository.getPendingFines();
        
        // Get overdue books
        List<ReportDTO.OverdueReportData.OverdueItem> overdueBooks = 
                dashboardRepository.getOverdueBooks().stream()
                        .map(row -> {
                            LocalDate dueDate = ((java.sql.Date) row[4]).toLocalDate();
                            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
                            
                            return ReportDTO.OverdueReportData.OverdueItem.builder()
                                    .bookTitle((String) row[1])
                                    .borrowerName((String) row[2])
                                    .borrowerEmail((String) row[3])
                                    .dueDate(dueDate)
                                    .daysOverdue(daysOverdue)
                                    .fineAmount((BigDecimal) row[5])
                                    .status((String) row[6])
                                    .build();
                        })
                        .collect(Collectors.toList());
        
        // Calculate users with overdue books
        Long usersWithOverdueBooks = overdueBooks.stream()
                .map(ReportDTO.OverdueReportData.OverdueItem::getBorrowerEmail)
                .distinct()
                .count();
        
        // Calculate overdue rate
        Long totalLoans = dashboardRepository.countTotalLoans();
        Double overdueRate = totalLoans > 0 ? 
                (totalOverdueItems.doubleValue() / totalLoans.doubleValue()) * 100 : 0.0;
        
        // Mock data for trend analysis
        List<ReportDTO.OverdueReportData.OverdueTrendItem> overdueTrend = Arrays.asList(
                ReportDTO.OverdueReportData.OverdueTrendItem.builder()
                        .period("This Month")
                        .overdueCount(totalOverdueItems)
                        .avgFineAmount(totalOutstandingFines.divide(BigDecimal.valueOf(Math.max(totalOverdueItems, 1)), RoundingMode.HALF_UP))
                        .build()
        );
        
        return ReportDTO.OverdueReportData.builder()
                .totalOverdueItems(totalOverdueItems)
                .totalOutstandingFines(totalOutstandingFines)
                .usersWithOverdueBooks(usersWithOverdueBooks)
                .overdueRate(overdueRate)
                .overdueBooks(overdueBooks)
                .usersWithMostOverdue(Collections.emptyList()) // Would need additional query
                .overdueTrend(overdueTrend)
                .build();
    }
    
    private Object generateReportData(ReportDTO.ReportRequest request) {
        switch (request.getReportType()) {
            case LOAN_REPORT:
                return generateLoanReport(request.getStartDate(), request.getEndDate());
            case SALES_REPORT:
                return generateSalesReport(request.getStartDate(), request.getEndDate());
            case USER_ACTIVITY_REPORT:
                return generateUserActivityReport(request.getStartDate(), request.getEndDate());
            case OVERDUE_REPORT:
                return generateOverdueReport();
            default:
                throw new IllegalArgumentException("Unsupported report type: " + request.getReportType());
        }
    }
    
    private String generateFileName(ReportDTO.ReportRequest request) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = getFileExtension(request.getExportFormat());
        String reportName = request.getReportType().name().toLowerCase().replace("_", "-");
        
        return String.format("%s-report-%s.%s", reportName, timestamp, extension);
    }
    
    private String getFileExtension(ReportDTO.ReportRequest.ExportFormat format) {
        if (format == null) return "pdf";
        
        switch (format) {
            case PDF: return "pdf";
            case EXCEL: return "xlsx";
            case CSV: return "csv";
            case JSON: return "json";
            default: return "pdf";
        }
    }
    
    private String getDefaultReportTitle(ReportDTO.ReportRequest.ReportType reportType) {
        switch (reportType) {
            case LOAN_REPORT: return "Library Loan Report";
            case SALES_REPORT: return "Sales Performance Report";
            case USER_ACTIVITY_REPORT: return "User Activity Analysis";
            case OVERDUE_REPORT: return "Overdue Books Report";
            case REVENUE_REPORT: return "Revenue Analysis Report";
            case INVENTORY_REPORT: return "Inventory Status Report";
            case FINANCIAL_SUMMARY: return "Financial Summary Report";
            default: return "Library Management Report";
        }
    }
    
    private Long calculateEstimatedFileSize(ReportDTO.ReportRequest request) {
        // Estimate file size based on report type and format
        long baseSize = 50000; // 50KB base
        
        switch (request.getReportType()) {
            case LOAN_REPORT:
            case SALES_REPORT:
                baseSize *= 2;
                break;
            case USER_ACTIVITY_REPORT:
                baseSize *= 1.5;
                break;
            default:
                break;
        }
        
        if (request.getExportFormat() == ReportDTO.ReportRequest.ExportFormat.EXCEL) {
            baseSize *= 1.3;
        } else if (request.getExportFormat() == ReportDTO.ReportRequest.ExportFormat.PDF) {
            baseSize *= 2;
        }
        
        return baseSize;
    }
    
    private Double calculatePercentage(Number value, Number total) {
        if (total == null || total.doubleValue() == 0) return 0.0;
        return (value.doubleValue() / total.doubleValue()) * 100.0;
    }
    
    private Double calculatePercentage(BigDecimal value, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return value.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
    }
}