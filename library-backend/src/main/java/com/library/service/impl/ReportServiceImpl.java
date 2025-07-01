package com.library.service.impl;

import com.library.dto.ReportRequestDTO;
import com.library.dto.ReportResponseDTO;
import com.library.entity.*;
import com.library.repository.*;
import com.library.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {
    
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final OrderRepository orderRepository;
    private final DocumentRepository documentRepository;
    
    private final Map<String, ReportResponseDTO> reportCache = new HashMap<>();
    
    @Override
    public ReportResponseDTO generateReport(ReportRequestDTO request) {
        log.info("Generating report: {}", request.getReportType());
        
        String reportId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        try {
            byte[] reportData = null;
            int recordCount = 0;
            
            switch (request.getReportType().toUpperCase()) {
                case "LOAN_REPORT":
                    if ("EXCEL".equalsIgnoreCase(request.getFormat())) {
                        reportData = generateLoanReportExcel(request);
                        recordCount = getLoanRecordCount(request);
                    }
                    break;
                case "ORDER_REPORT":
                    if ("EXCEL".equalsIgnoreCase(request.getFormat())) {
                        reportData = generateOrderReportExcel(request);
                        recordCount = getOrderRecordCount(request);
                    }
                    break;
                case "USER_REPORT":
                    if ("EXCEL".equalsIgnoreCase(request.getFormat())) {
                        reportData = generateUserReportExcel(request);
                        recordCount = getUserRecordCount(request);
                    }
                    break;
                case "FINANCIAL_REPORT":
                    if ("EXCEL".equalsIgnoreCase(request.getFormat())) {
                        reportData = generateFinancialReportExcel(request);
                        recordCount = getFinancialRecordCount(request);
                    }
                    break;
                case "BOOK_REPORT":
                    if ("EXCEL".equalsIgnoreCase(request.getFormat())) {
                        reportData = generateBookReportExcel(request);
                        recordCount = getBookRecordCount(request);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported report type: " + request.getReportType());
            }
            
            if (reportData == null) {
                throw new IllegalArgumentException("Unsupported format: " + request.getFormat());
            }
            
            // Save report data (in real implementation, save to file system or cloud storage)
            String fileName = generateFileName(request);
            
            ReportResponseDTO response = ReportResponseDTO.builder()
                    .reportId(reportId)
                    .reportType(request.getReportType())
                    .format(request.getFormat())
                    .title(request.getTitle() != null ? request.getTitle() : getDefaultTitle(request.getReportType()))
                    .fileName(fileName)
                    .downloadUrl("/api/v1/admin/reports/" + reportId + "/download")
                    .fileSize((long) reportData.length)
                    .fileSizeFormatted(formatFileSize(reportData.length))
                    .status("COMPLETED")
                    .generatedAt(now)
                    .expiresAt(now.plusDays(7)) // Reports expire after 7 days
                    .generatedBy(getCurrentUserId())
                    .recordCount(recordCount)
                    .build();
            
            // Cache the response and data
            reportCache.put(reportId, response);
            
            log.info("Report generated successfully: {}", reportId);
            return response;
            
        } catch (Exception e) {
            log.error("Failed to generate report: {}", e.getMessage(), e);
            
            ReportResponseDTO errorResponse = ReportResponseDTO.builder()
                    .reportId(reportId)
                    .reportType(request.getReportType())
                    .format(request.getFormat())
                    .status("FAILED")
                    .generatedAt(now)
                    .generatedBy(getCurrentUserId())
                    .errorMessage(e.getMessage())
                    .build();
            
            reportCache.put(reportId, errorResponse);
            return errorResponse;
        }
    }
    
    @Override
    public ReportResponseDTO getReportStatus(String reportId) {
        ReportResponseDTO report = reportCache.get(reportId);
        if (report == null) {
            throw new IllegalArgumentException("Report not found: " + reportId);
        }
        return report;
    }
    
    @Override
    public List<ReportResponseDTO> getAvailableReports() {
        String currentUserId = getCurrentUserId();
        return reportCache.values().stream()
                .filter(report -> currentUserId.equals(report.getGeneratedBy()))
                .filter(report -> report.getExpiresAt().isAfter(LocalDateTime.now()))
                .sorted((a, b) -> b.getGeneratedAt().compareTo(a.getGeneratedAt()))
                .collect(Collectors.toList());
    }
    
    @Override
    public String getReportDownloadUrl(String reportId) {
        ReportResponseDTO report = getReportStatus(reportId);
        if (!"COMPLETED".equals(report.getStatus())) {
            throw new IllegalStateException("Report is not ready for download");
        }
        return report.getDownloadUrl();
    }
    
    @Override
    public void deleteReport(String reportId) {
        reportCache.remove(reportId);
        // In real implementation, also delete the file from storage
    }
    
    @Override
    public List<String> getSupportedReportTypes() {
        return Arrays.asList(
                "LOAN_REPORT",
                "ORDER_REPORT", 
                "USER_REPORT",
                "FINANCIAL_REPORT",
                "BOOK_REPORT",
                "DOCUMENT_REPORT"
        );
    }
    
    @Override
    public List<String> getSupportedFormats() {
        return Arrays.asList("EXCEL", "PDF", "CSV");
    }
    
    // Report generation methods
    
    private byte[] generateLoanReportExcel(ReportRequestDTO request) throws IOException {
        List<Loan> loans = getLoanData(request);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Loan Report");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Loan ID", "User", "Book Title", "Author", "Loan Date", "Due Date", 
                               "Return Date", "Status", "Fine Amount", "Days Overdue"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            
            // Add data rows
            int rowNum = 1;
            for (Loan loan : loans) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(loan.getId());
                row.createCell(1).setCellValue(loan.getUser().getUsername());
                row.createCell(2).setCellValue(loan.getBook().getTitle());
                row.createCell(3).setCellValue(loan.getBook().getAuthors().stream()
                        .map(Author::getName)
                        .collect(Collectors.joining(", ")));
                row.createCell(4).setCellValue(loan.getLoanDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                row.createCell(5).setCellValue(loan.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                row.createCell(6).setCellValue(loan.getReturnDate() != null ? 
                        loan.getReturnDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
                row.createCell(7).setCellValue(loan.getStatus().toString());
                row.createCell(8).setCellValue(loan.getFineAmount() != null ? 
                        loan.getFineAmount().doubleValue() : 0.0);
                row.createCell(9).setCellValue(loan.getDaysOverdue() != null ? loan.getDaysOverdue() : 0);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    private byte[] generateOrderReportExcel(ReportRequestDTO request) throws IOException {
        List<Order> orders = getOrderData(request);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Order Report");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Order ID", "Order Code", "User", "Order Date", "Status", 
                               "Payment Status", "Total Amount", "Shipping Fee", "Tax Amount", "Items Count"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            
            // Add data rows
            int rowNum = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(order.getOrderCode());
                row.createCell(2).setCellValue(order.getUser().getUsername());
                row.createCell(3).setCellValue(order.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE));
                row.createCell(4).setCellValue(order.getStatus().toString());
                row.createCell(5).setCellValue(order.getPaymentStatus().toString());
                row.createCell(6).setCellValue(order.getTotalAmount().doubleValue());
                row.createCell(7).setCellValue(order.getShippingFee().doubleValue());
                row.createCell(8).setCellValue(order.getTaxAmount().doubleValue());
                row.createCell(9).setCellValue(order.getOrderItems().size());
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    private byte[] generateUserReportExcel(ReportRequestDTO request) throws IOException {
        List<User> users = getUserData(request);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("User Report");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"User ID", "Username", "Email", "Full Name", "Phone", 
                               "Registration Date", "Status", "Role", "Total Loans", "Total Orders"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            
            // Add data rows
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getFullName() != null ? user.getFullName() : "");
                row.createCell(4).setCellValue(user.getPhone() != null ? user.getPhone() : "");
                row.createCell(5).setCellValue(user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE));
                row.createCell(6).setCellValue(user.getActive() ? "Active" : "Inactive");
                row.createCell(7).setCellValue(user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.joining(", ")));
                row.createCell(8).setCellValue(loanRepository.countByUserId(user.getId()));
                row.createCell(9).setCellValue(orderRepository.countByUserId(user.getId()));
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    private byte[] generateFinancialReportExcel(ReportRequestDTO request) throws IOException {
        // Get financial data
        BigDecimal totalRevenue = orderRepository.sumRevenueBetween(
                request.getStartDate(), request.getEndDate());
        BigDecimal totalFines = loanRepository.sumFinesBetween(
                request.getStartDate(), request.getEndDate());
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet summarySheet = workbook.createSheet("Financial Summary");
            
            // Summary section
            int rowNum = 0;
            Row titleRow = summarySheet.createRow(rowNum++);
            titleRow.createCell(0).setCellValue("Financial Report Summary");
            
            rowNum++; // Empty row
            
            Row periodRow = summarySheet.createRow(rowNum++);
            periodRow.createCell(0).setCellValue("Period:");
            periodRow.createCell(1).setCellValue(
                    request.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + 
                    " to " + 
                    request.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            
            rowNum++; // Empty row
            
            Row revenueRow = summarySheet.createRow(rowNum++);
            revenueRow.createCell(0).setCellValue("Total Revenue:");
            revenueRow.createCell(1).setCellValue(totalRevenue != null ? totalRevenue.doubleValue() : 0.0);
            
            Row finesRow = summarySheet.createRow(rowNum++);
            finesRow.createCell(0).setCellValue("Total Fines:");
            finesRow.createCell(1).setCellValue(totalFines != null ? totalFines.doubleValue() : 0.0);
            
            Row totalRow = summarySheet.createRow(rowNum++);
            totalRow.createCell(0).setCellValue("Total Income:");
            totalRow.createCell(1).setCellValue(
                    (totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                            .add(totalFines != null ? totalFines : BigDecimal.ZERO)
                            .doubleValue());
            
            // Auto-size columns
            summarySheet.autoSizeColumn(0);
            summarySheet.autoSizeColumn(1);
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    private byte[] generateBookReportExcel(ReportRequestDTO request) throws IOException {
        List<Book> books = getBookData(request);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Book Report");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Book ID", "Title", "Authors", "Category", "Publisher", 
                               "ISBN", "Stock Quantity", "Available", "Total Borrows", "Total Purchases"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            
            // Add data rows
            int rowNum = 1;
            for (Book book : books) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(book.getId());
                row.createCell(1).setCellValue(book.getTitle());
                row.createCell(2).setCellValue(book.getAuthors().stream()
                        .map(Author::getName)
                        .collect(Collectors.joining(", ")));
                row.createCell(3).setCellValue(book.getCategory() != null ? book.getCategory().getName() : "");
                row.createCell(4).setCellValue(book.getPublisher() != null ? book.getPublisher().getName() : "");
                row.createCell(5).setCellValue(book.getIsbn() != null ? book.getIsbn() : "");
                row.createCell(6).setCellValue(book.getStockQuantity());
                row.createCell(7).setCellValue(book.getAvailableQuantity());
                row.createCell(8).setCellValue(book.getTotalBorrows() != null ? book.getTotalBorrows() : 0);
                row.createCell(9).setCellValue(book.getTotalPurchases() != null ? book.getTotalPurchases() : 0);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    // Helper methods for data retrieval
    
    private List<Loan> getLoanData(ReportRequestDTO request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            return loanRepository.findLoansBetweenDates(request.getStartDate(), request.getEndDate());
        }
        return loanRepository.findAll();
    }
    
    private List<Order> getOrderData(ReportRequestDTO request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            return orderRepository.findOrdersBetweenDates(request.getStartDate(), request.getEndDate());
        }
        return orderRepository.findAll();
    }
    
    private List<User> getUserData(ReportRequestDTO request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            return userRepository.findUsersBetweenDates(request.getStartDate(), request.getEndDate());
        }
        return userRepository.findAll();
    }
    
    private List<Book> getBookData(ReportRequestDTO request) {
        return bookRepository.findAll();
    }
    
    // Helper methods for record counts
    
    private int getLoanRecordCount(ReportRequestDTO request) {
        return getLoanData(request).size();
    }
    
    private int getOrderRecordCount(ReportRequestDTO request) {
        return getOrderData(request).size();
    }
    
    private int getUserRecordCount(ReportRequestDTO request) {
        return getUserData(request).size();
    }
    
    private int getFinancialRecordCount(ReportRequestDTO request) {
        return 1; // Summary report
    }
    
    private int getBookRecordCount(ReportRequestDTO request) {
        return getBookData(request).size();
    }
    
    // Utility methods
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    private String generateFileName(ReportRequestDTO request) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = request.getFormat().toLowerCase().equals("excel") ? "xlsx" : 
                          request.getFormat().toLowerCase();
        return request.getReportType().toLowerCase() + "_" + timestamp + "." + extension;
    }
    
    private String getDefaultTitle(String reportType) {
        switch (reportType.toUpperCase()) {
            case "LOAN_REPORT": return "Loan Management Report";
            case "ORDER_REPORT": return "Order Management Report";
            case "USER_REPORT": return "User Management Report";
            case "FINANCIAL_REPORT": return "Financial Summary Report";
            case "BOOK_REPORT": return "Book Inventory Report";
            default: return "System Report";
        }
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    private String getCurrentUserId() {
        // This would get the current authenticated user ID
        return "admin"; // Placeholder
    }
}