# 📚 Hệ thống Quản lý Thư viện - Hướng dẫn Toàn diện về Events & Schedulers

## 📋 Mục lục
1. [Tổng quan](#tổng-quan)
2. [Kiến trúc Event-Driven](#kiến-trúc-event-driven)
3. [Danh mục tất cả Events](#danh-mục-tất-cả-events)
4. [Các tác vụ định kỳ](#các-tác-vụ-định-kỳ)
5. [Tích hợp Event-Schedule](#tích-hợp-event-schedule)
6. [Ví dụ triển khai](#ví-dụ-triển-khai)
7. [Giám sát & Thống kê](#giám-sát--thống-kê)

---

## 🎯 Tổng quan

Hệ thống Quản lý Thư viện sử dụng Kiến trúc hướng sự kiện (Event-Driven Architecture) kết hợp với Tác vụ định kỳ (Scheduled Tasks) để tự động hóa quy trình, tăng hiệu suất và cải thiện trải nghiệm người dùng.

### Lợi ích chính:
- **Tách rời (Decoupling)**: Các module hoạt động độc lập
- **Khả năng mở rộng (Scalability)**: Dễ dàng mở rộng hệ thống
- **Độ tin cậy (Reliability)**: Xử lý lỗi hiệu quả
- **Hiệu suất (Performance)**: Xử lý không chặn luồng chính
- **Khả năng bảo trì (Maintainability)**: Code dễ bảo trì và phát triển

---

## 🏗️ Kiến trúc Event-Driven

### Các thành phần cốt lõi:

```java
// 1. Trình phát sự kiện (Event Publisher)
@Component
public class EventPublisher {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public void publish(Object event) {
        eventPublisher.publishEvent(event);
    }
}

// 2. Trình lắng nghe sự kiện (Event Listener)
@Component
public class EventListener {
    @EventListener
    @Async
    public void handle(ApplicationEvent event) {
        // Xử lý sự kiện
    }
}

// 3. Cấu hình sự kiện (Event Configuration)
@Configuration
@EnableAsync
public class EventConfig {
    @Bean
    public Executor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("event-");
        return executor;
    }
}
```

---

## 📊 Danh mục tất cả Events

### 1. **Các sự kiện Mượn/Trả sách (Loan Events)**

| Sự kiện | Kích hoạt bởi | Mục đích | Độ ưu tiên | Công việc thực hiện |
|---------|---------------|----------|------------|---------------------|
| **LoanRequestEvent** | Người dùng tạo yêu cầu mượn | Thông báo thủ thư | Trung bình (2) | - Thông báo tất cả thủ thư<br>- Cập nhật thống kê dashboard<br>- Gửi xác nhận cho người dùng<br>- Ghi nhật ký kiểm toán |
| **LoanApprovedEvent** | Thủ thư phê duyệt | Thông báo người dùng | Cao (3) | - Thông báo người dùng<br>- Cập nhật tồn kho sách<br>- Lên lịch nhắc nhở hạn trả<br>- Tạo mã nhận sách |
| **LoanRejectedEvent** | Thủ thư từ chối | Thông báo người dùng | Cao (3) | - Thông báo người dùng kèm lý do<br>- Hủy đặt chỗ sách<br>- Cập nhật thống kê người dùng |
| **LoanBorrowedEvent** | Người dùng nhận sách | Xác nhận mượn | Trung bình (2) | - Bắt đầu thời gian mượn<br>- Cập nhật tồn kho<br>- Lên lịch nhắc nhở trả |
| **LoanDueSoonEvent** | Hệ thống tự động | Nhắc nhở hạn trả | Trung bình-Cao | - Gửi thông báo nhắc nhở<br>- Gửi email nếu được bật<br>- Cập nhật cảnh báo dashboard |
| **LoanOverdueEvent** | Hệ thống tự động | Cảnh báo quá hạn | Nghiêm trọng (4) | - Tính tiền phạt<br>- Chặn mượn mới<br>- Thông báo user & admin<br>- Cập nhật điểm tín dụng |
| **LoanReturnedEvent** | Thủ thư xác nhận | Hoàn tất trả sách | Trung bình (2) | - Cập nhật tồn kho<br>- Tính tiền phạt cuối<br>- Cập nhật lịch sử người dùng<br>- Cho phép người khác mượn |
| **LoanRenewedEvent** | Người dùng/Thủ thư | Gia hạn mượn | Trung bình (2) | - Gia hạn ngày trả<br>- Đặt lại lời nhắc<br>- Ghi lịch sử gia hạn |

#### Ví dụ triển khai:
```java
@Component
@Slf4j
public class LoanEventHandlers {
    
    @EventListener
    @Async("loanEventExecutor")
    public void handleLoanRequest(LoanRequestEvent event) {
        // 1. Thông báo các thủ thư
        List<User> librarians = userRepository.findLibrarians();
        librarians.forEach(librarian -> {
            notificationService.create(NotificationDTO.builder()
                .userId(librarian.getId())
                .title("Yêu cầu mượn sách mới")
                .message(String.format("%s yêu cầu mượn '%s'", 
                    event.getUserName(), event.getBookTitle()))
                .type(NotificationType.LOAN_REQUEST)
                .priority(2)
                .actionUrl("/admin/loans/pending/" + event.getLoanId())
                .build());
        });
        
        // 2. Cập nhật dashboard
        dashboardService.incrementPendingLoans();
        
        // 3. Gửi xác nhận cho người dùng
        notificationService.create(NotificationDTO.builder()
            .userId(event.getUserId())
            .title("Yêu cầu đã được gửi")
            .message("Yêu cầu mượn sách của bạn đang được xem xét")
            .build());
        
        // 4. Ghi nhật ký kiểm toán
        auditService.log(AuditEntry.builder()
            .action("LOAN_REQUESTED")
            .userId(event.getUserId())
            .entityId(event.getLoanId())
            .timestamp(LocalDateTime.now())
            .build());
    }
    
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLoanApproved(LoanApprovedEvent event) {
        // 1. Tạo mã nhận sách
        String pickupCode = generatePickupCode();
        loanRepository.updatePickupCode(event.getLoanId(), pickupCode);
        
        // 2. Đặt chỗ sách
        inventoryService.reserveBook(event.getBookId());
        
        // 3. Lên lịch nhắc nhở
        schedulerService.scheduleDueReminder(event.getLoanId(), event.getDueDate());
        
        // 4. Gửi thông báo
        notificationService.sendLoanApprovalNotification(
            event.getUserId(), event.getLoanId(), pickupCode
        );
    }
}
```

### 2. **Các sự kiện Đặt hàng (Order Events)**

| Sự kiện | Kích hoạt bởi | Mục đích | Độ ưu tiên | Công việc thực hiện |
|---------|---------------|----------|------------|---------------------|
| **OrderPlacedEvent** | Người dùng đặt hàng | Xác nhận đơn hàng | Trung bình (2) | - Gửi xác nhận<br>- Đặt chỗ tồn kho<br>- Khởi tạo thanh toán<br>- Thông báo kho hàng |
| **OrderPaidEvent** | Thanh toán thành công | Cập nhật trạng thái | Cao (3) | - Cập nhật trạng thái đơn<br>- Hoàn tất tồn kho<br>- Tạo hóa đơn<br>- Bắt đầu xử lý |
| **OrderShippedEvent** | Kho hàng gửi | Theo dõi vận chuyển | Trung bình (2) | - Gửi thông tin theo dõi<br>- Cập nhật thời gian giao<br>- Thông báo người dùng |
| **OrderDeliveredEvent** | Xác nhận giao hàng | Hoàn tất đơn | Trung bình (2) | - Hoàn tất đơn hàng<br>- Yêu cầu đánh giá<br>- Cập nhật thống kê |
| **OrderCancelledEvent** | Người dùng/Hệ thống hủy | Hủy đơn hàng | Cao (3) | - Giải phóng tồn kho<br>- Xử lý hoàn tiền<br>- Thông báo các bên<br>- Cập nhật báo cáo |

#### Triển khai:
```java
@Component
public class OrderEventHandlers {
    
    @EventListener
    @Order(1) // Process first
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // 1. Reserve inventory
        event.getItems().forEach(item -> {
            inventoryService.reserve(item.getBookId(), item.getQuantity());
        });
        
        // 2. Create payment request
        PaymentRequest payment = paymentService.createPaymentRequest(
            event.getOrderId(), 
            event.getTotalAmount()
        );
        
        // 3. Send notifications
        notificationService.sendOrderConfirmation(event);
        
        // 4. Notify warehouse
        warehouseService.notifyNewOrder(event);
        
        // 5. Start timeout timer (cancel if not paid in 24h)
        schedulerService.scheduleOrderTimeout(event.getOrderId(), 24);
    }
}
```

### 3. **Các sự kiện Thanh toán (Payment Events)**

| Sự kiện | Kích hoạt bởi | Mục đích | Độ ưu tiên | Công việc thực hiện |
|---------|---------------|----------|------------|---------------------|
| **PaymentInitiatedEvent** | Bắt đầu thanh toán | Ghi log | Thấp (1) | - Ghi lại thử nghiệm<br>- Tạo bản ghi giao dịch |
| **PaymentSuccessEvent** | VNPay callback | Xác nhận | Nghiêm trọng (4) | - Cập nhật đơn hàng<br>- Gửi biên lai<br>- Hoàn tất tồn kho<br>- Cập nhật kế toán |
| **PaymentFailedEvent** | Lỗi VNPay | Cảnh báo | Cao (3) | - Thông báo người dùng<br>- Ghi lý do thất bại<br>- Đề xuất thử lại<br>- Cảnh báo admin nếu nghi ngờ |
| **PaymentRefundedEvent** | Admin hoàn tiền | Xử lý | Cao (3) | - Cập nhật bản ghi<br>- Trả lại tồn kho<br>- Gửi xác nhận<br>- Cập nhật báo cáo |

### 4. **Các sự kiện Người dùng (User Events)**

| Sự kiện | Kích hoạt bởi | Mục đích | Độ ưu tiên | Công việc thực hiện |
|---------|---------------|----------|------------|---------------------|
| **UserRegisteredEvent** | Đăng ký mới | Chào mừng | Trung bình (2) | - Gửi email chào mừng<br>- Tạo cài đặt ban đầu<br>- Gán quyền mặc định<br>- Theo dõi nguồn |
| **UserActivatedEvent** | Xác thực email | Kích hoạt | Trung bình (2) | - Bật tài khoản<br>- Gửi xác nhận<br>- Áp dụng ưu đãi chào mừng |
| **PasswordChangedEvent** | Cập nhật bảo mật | Cảnh báo | Cao (3) | - Thông báo người dùng<br>- Ghi IP/thiết bị<br>- Hủy sessions<br>- Email bảo mật |
| **UserDeactivatedEvent** | Hành động admin | Vô hiệu hóa | Nghiêm trọng (4) | - Chặn truy cập<br>- Hủy các lần mượn<br>- Thông báo người dùng<br>- Ghi lý do |

### 5. **Các sự kiện Sách (Book Events)**

| Sự kiện | Kích hoạt bởi | Mục đích | Độ ưu tiên | Công việc thực hiện |
|---------|---------------|----------|------------|---------------------|
| **BookAddedEvent** | Sách mới | Thông báo | Thấp (1) | - Cập nhật danh mục<br>- Thông báo người đăng ký<br>- Cập nhật chỉ mục tìm kiếm<br>- Tạo đề xuất |
| **BookUpdatedEvent** | Chỉnh sửa thông tin | Đồng bộ | Thấp (1) | - Cập nhật cache<br>- Đánh chỉ mục lại<br>- Thông báo người dùng liên quan |
| **BookAvailableEvent** | Trả/Nhập kho | Cảnh báo | Trung bình (2) | - Thông báo danh sách chờ<br>- Cập nhật tình trạng<br>- Kích hoạt đề xuất |
| **BookReservedEvent** | Người dùng đặt chỗ | Giữ chỗ | Trung bình (2) | - Giữ tồn kho<br>- Đặt thời hạn<br>- Thông báo người dùng<br>- Cập nhật hàng đợi |

### 6. **Các sự kiện Hệ thống (System Events)**

| Sự kiện | Kích hoạt bởi | Mục đích | Độ ưu tiên | Công việc thực hiện |
|---------|---------------|----------|------------|---------------------|
| **SystemMaintenanceEvent** | Lịch trình admin | Cảnh báo | Nghiêm trọng (4) | - Phát sóng thông báo<br>- Đặt chế độ bảo trì<br>- Ghi sessions đang hoạt động |
| **SecurityAlertEvent** | Phát hiện bất thường | Cảnh báo | Nghiêm trọng (4) | - Cảnh báo admins<br>- Ghi chi tiết<br>- Thực hiện hành động bảo vệ<br>- Tạo báo cáo |
| **PerformanceAlertEvent** | Vượt ngưỡng | Giám sát | Cao (3) | - Cảnh báo DevOps<br>- Mở rộng tài nguyên<br>- Ghi metrics<br>- Kích hoạt chẩn đoán |

---

## ⏰ Các tác vụ định kỳ

### 1. **Bộ định thời Thông báo (Notification Schedulers)**

#### **Bộ định thời Nhắc nhở Hạn trả**
```java
@Component
@EnableScheduling
public class DueDateReminderScheduler {
    
    @Scheduled(cron = "0 0 9,15,21 * * *") // 9 giờ sáng, 3 giờ chiều, 9 giờ tối hàng ngày
    public void sendDueDateReminders() {
        log.info("Đang bắt đầu tác vụ nhắc nhở hạn trả");
        
        // Lấy các lần mượn sắp đến hạn trong các khoảng thời gian khác nhau
        Map<Integer, List<Loan>> loansByDueDate = new HashMap<>();
        loansByDueDate.put(1, loanRepository.findDueTomorrow());
        loansByDueDate.put(3, loanRepository.findDueInDays(3));
        loansByDueDate.put(7, loanRepository.findDueInDays(7));
        
        // Xử lý từng nhóm với độ ưu tiên khác nhau
        loansByDueDate.forEach((days, loans) -> {
            loans.forEach(loan -> {
                // Kiểm tra tần suất thông báo
                if (!hasRecentReminder(loan, days)) {
                    LoanDueSoonEvent event = new LoanDueSoonEvent(loan, days);
                    event.setPriority(days == 1 ? 3 : 2); // Ưu tiên cao hơn cho ngày mai
                    eventPublisher.publishEvent(event);
                }
            });
        });
        
        log.info("Đã xử lý {} lần mượn cho nhắc nhở hạn trả", 
            loansByDueDate.values().stream().mapToInt(List::size).sum());
    }
    
    private boolean hasRecentReminder(Loan loan, int daysUntilDue) {
        // Tần suất thông minh: tránh spam
        int hoursSinceLastReminder = switch (daysUntilDue) {
            case 1 -> 12;  // Mỗi 12 giờ cho ngày mai
            case 3 -> 48;  // Mỗi 2 ngày cho 3 ngày tới
            case 7 -> 96;  // Mỗi 4 ngày cho tuần tới
            default -> 168; // Hàng tuần cho các trường hợp khác
        };
        
        return notificationRepository.existsRecentNotification(
            loan.getUser().getId(),
            NotificationType.LOAN_DUE_SOON,
            loan.getId(),
            LocalDateTime.now().minusHours(hoursSinceLastReminder)
        );
    }
}
```

#### **Bộ định thời Phát hiện Quá hạn**
```java
@Component
public class OverdueDetectionScheduler {
    
    @Scheduled(cron = "0 0 0,6,12,18 * * *") // Mỗi 6 giờ
    public void detectAndProcessOverdueLoans() {
        log.info("Đang bắt đầu phát hiện quá hạn");
        
        // Tìm tất cả các lần mượn quá hạn
        List<Loan> overdueLoans = loanRepository.findOverdueLoans();
        
        overdueLoans.forEach(loan -> {
            // Cập nhật trạng thái nếu cần
            if (loan.getStatus() != LoanStatus.OVERDUE) {
                loan.setStatus(LoanStatus.OVERDUE);
                loan.setOverdueSince(LocalDateTime.now());
                loanRepository.save(loan);
            }
            
            // Tính tiền phạt
            BigDecimal fine = fineCalculator.calculate(loan);
            loan.setCurrentFine(fine);
            
            // Xác định tần suất thông báo dựa trên thời gian quá hạn
            long daysOverdue = ChronoUnit.DAYS.between(
                loan.getDueDate(), LocalDate.now()
            );
            
            if (shouldNotifyOverdue(loan, daysOverdue)) {
                LoanOverdueEvent event = new LoanOverdueEvent(loan, daysOverdue, fine);
                eventPublisher.publishEvent(event);
            }
        });
        
        // Cập nhật metrics dashboard
        metricsService.updateOverdueMetrics(overdueLoans.size());
        
        log.info("Đã xử lý {} lần mượn quá hạn", overdueLoans.size());
    }
    
    private boolean shouldNotifyOverdue(Loan loan, long daysOverdue) {
        // Chiến lược thông báo tầng dần
        if (daysOverdue <= 7) {
            // Tuần đầu: thông báo hàng ngày
            return !hasNotificationInLast(loan, 24);
        } else if (daysOverdue <= 30) {
            // Tháng đầu: mỗi 3 ngày
            return !hasNotificationInLast(loan, 72);
        } else {
            // Sau tháng: hàng tuần
            return !hasNotificationInLast(loan, 168);
        }
    }
}
```

### 2. **Bộ định thời Bảo trì Hệ thống**

#### **Bộ định thời Dọn dẹp Cơ sở dữ liệu**
```java
@Component
public class DatabaseCleanupScheduler {
    
    @Scheduled(cron = "0 0 2 * * *") // 2 AM daily
    public void performDatabaseCleanup() {
        log.info("Starting database cleanup");
        
        // 1. Archive old notifications
        int archivedNotifications = notificationRepository.archiveOldNotifications(
            LocalDateTime.now().minusDays(30)
        );
        
        // 2. Delete expired sessions
        int deletedSessions = sessionRepository.deleteExpiredSessions();
        
        // 3. Clean up abandoned carts
        int cleanedCarts = cartRepository.cleanAbandonedCarts(
            LocalDateTime.now().minusDays(7)
        );
        
        // 4. Remove old audit logs
        int removedLogs = auditRepository.removeOldLogs(
            LocalDateTime.now().minusDays(90)
        );
        
        // 5. Optimize tables
        databaseService.optimizeTables();
        
        log.info("Cleanup complete - Notifications: {}, Sessions: {}, Carts: {}, Logs: {}",
            archivedNotifications, deletedSessions, cleanedCarts, removedLogs);
        
        // Send summary to admin
        systemEventPublisher.publishEvent(
            new MaintenanceCompletedEvent("Database Cleanup", getCleanupSummary())
        );
    }
}
```

#### **Cache Warming Scheduler**
```java
@Component
public class CacheWarmingScheduler {
    
    @Scheduled(cron = "0 0 6 * * *") // 6 AM daily
    public void warmUpCaches() {
        log.info("Starting cache warming");
        
        // 1. Popular books cache
        List<Book> popularBooks = bookRepository.findTop100ByOrderByViewCountDesc();
        popularBooks.forEach(book -> cacheService.put("book:" + book.getId(), book));
        
        // 2. Active users cache
        List<User> activeUsers = userRepository.findActiveUsers();
        activeUsers.forEach(user -> cacheService.put("user:" + user.getId(), user));
        
        // 3. Category tree cache
        CategoryTree categoryTree = categoryService.buildCategoryTree();
        cacheService.put("category:tree", categoryTree);
        
        // 4. Dashboard statistics
        DashboardStats stats = dashboardService.calculateStats();
        cacheService.put("dashboard:stats", stats);
        
        log.info("Cache warming completed");
    }
}
```

### 3. **Business Analytics Schedulers**

#### **Weekly Report Generator**
```java
@Component
public class WeeklyReportScheduler {
    
    @Scheduled(cron = "0 0 8 * * MON") // Monday 8 AM
    public void generateWeeklyReports() {
        log.info("Generating weekly reports");
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        // 1. Loan statistics
        LoanStats loanStats = statisticsService.calculateLoanStats(startDate, endDate);
        
        // 2. Order statistics  
        OrderStats orderStats = statisticsService.calculateOrderStats(startDate, endDate);
        
        // 3. User activity
        UserActivityStats userStats = statisticsService.calculateUserStats(startDate, endDate);
        
        // 4. Financial summary
        FinancialSummary financial = financialService.calculateWeeklySummary(startDate, endDate);
        
        // Generate report
        WeeklyReport report = WeeklyReport.builder()
            .period(startDate.toLocalDate() + " to " + endDate.toLocalDate())
            .loanStats(loanStats)
            .orderStats(orderStats)
            .userStats(userStats)
            .financial(financial)
            .generatedAt(LocalDateTime.now())
            .build();
        
        // Save report
        reportRepository.save(report);
        
        // Send to administrators
        List<User> admins = userRepository.findAdministrators();
        admins.forEach(admin -> {
            notificationService.sendWeeklyReport(admin, report);
        });
        
        log.info("Weekly reports sent to {} administrators", admins.size());
    }
}
```

#### **Recommendation Engine Scheduler**
```java
@Component
public class RecommendationScheduler {
    
    @Scheduled(cron = "0 0 22 * * *") // 10 PM daily
    public void generateUserRecommendations() {
        log.info("Starting recommendation generation");
        
        // Get active users
        List<User> activeUsers = userRepository.findUsersActiveInLast(30);
        
        activeUsers.parallelStream().forEach(user -> {
            try {
                // Analyze user history
                UserProfile profile = profileAnalyzer.analyze(user);
                
                // Generate recommendations
                List<Book> recommendations = recommendationEngine.recommend(profile, 10);
                
                // Save recommendations
                userRecommendationRepository.saveRecommendations(user.getId(), recommendations);
                
                // Send notification if user opted in
                if (user.isRecommendationNotificationEnabled()) {
                    eventPublisher.publishEvent(
                        new RecommendationsReadyEvent(user, recommendations)
                    );
                }
            } catch (Exception e) {
                log.error("Error generating recommendations for user: " + user.getId(), e);
            }
        });
        
        log.info("Generated recommendations for {} users", activeUsers.size());
    }
}
```

### 4. **Integration Schedulers**

#### **External API Sync Scheduler**
```java
@Component
public class ExternalApiSyncScheduler {
    
    @Scheduled(cron = "0 0 3 * * *") // 3 AM daily
    public void syncWithExternalSystems() {
        log.info("Starting external systems sync");
        
        // 1. Sync book metadata from ISBN database
        List<Book> booksNeedingMetadata = bookRepository.findBooksWithIncompleteMetadata();
        booksNeedingMetadata.forEach(book -> {
            try {
                BookMetadata metadata = isbnService.fetchMetadata(book.getIsbn());
                bookService.updateMetadata(book, metadata);
            } catch (Exception e) {
                log.error("Failed to sync metadata for book: " + book.getId(), e);
            }
        });
        
        // 2. Update exchange rates for pricing
        Map<String, BigDecimal> rates = currencyService.fetchLatestRates();
        priceService.updateExchangeRates(rates);
        
        // 3. Sync with accounting system
        List<Order> unsyncedOrders = orderRepository.findUnsyncedOrders();
        accountingService.syncOrders(unsyncedOrders);
        
        log.info("External sync completed");
    }
}
```

---

## 🔄 Tích hợp Event-Schedule

### Ví dụ: Chu trình Mượn sách Hoàn chỉnh

```java
// 1. User creates loan request (Event)
public class LoanController {
    @PostMapping("/request")
    public LoanDTO requestLoan(@RequestBody LoanRequestDTO request) {
        Loan loan = loanService.createRequest(request);
        eventPublisher.publishEvent(new LoanRequestEvent(loan)); // Trigger notifications
        return loan;
    }
}

// 2. Scheduler monitors due dates
@Scheduled(cron = "0 0 9 * * *")
public void checkDueDates() {
    List<Loan> dueSoon = loanRepository.findDueSoon();
    dueSoon.forEach(loan -> 
        eventPublisher.publishEvent(new LoanDueSoonEvent(loan))
    );
}

// 3. Event handler creates notifications
@EventListener
public void handleLoanDueSoon(LoanDueSoonEvent event) {
    notificationService.createDueReminder(event.getLoan());
}

// 4. Another scheduler detects overdue
@Scheduled(cron = "0 0 0 * * *")
public void detectOverdue() {
    List<Loan> overdue = loanRepository.findNewlyOverdue();
    overdue.forEach(loan -> {
        loan.setStatus(LoanStatus.OVERDUE);
        eventPublisher.publishEvent(new LoanOverdueEvent(loan));
    });
}
```

---

## 📈 Monitoring & Metrics

### Event Metrics Collection

```java
@Component
public class EventMetricsCollector {
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void collectMetrics(ApplicationEvent event) {
        // Count events by type
        meterRegistry.counter("events.published",
            "type", event.getClass().getSimpleName()
        ).increment();
        
        // Track processing time
        Timer.Sample sample = Timer.start(meterRegistry);
        processEvent(event);
        sample.stop(meterRegistry.timer("events.processing.duration",
            "type", event.getClass().getSimpleName()
        ));
    }
}
```

### Scheduler Metrics

```java
@Aspect
@Component
public class SchedulerMetricsAspect {
    
    @Around("@annotation(scheduled)")
    public Object measureScheduledTask(ProceedingJoinPoint joinPoint, Scheduled scheduled) throws Throwable {
        String taskName = joinPoint.getSignature().getName();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Object result = joinPoint.proceed();
            
            meterRegistry.counter("scheduler.executions",
                "task", taskName,
                "status", "success"
            ).increment();
            
            return result;
        } catch (Exception e) {
            meterRegistry.counter("scheduler.executions",
                "task", taskName,
                "status", "failure"
            ).increment();
            throw e;
        } finally {
            sample.stop(meterRegistry.timer("scheduler.duration", "task", taskName));
        }
    }
}
```

---

## 🎯 Thực hành Tốt nhất

### 1. **Thiết kế Event**
- Giữ các events không thay đổi (immutable)
- Bao gồm tất cả dữ liệu cần thiết
- Sử dụng quy ước đặt tên rõ ràng
- Đánh phiên bản events để tương thích

### 2. **Cấu hình Scheduler**
- Sử dụng biểu thức cron phù hợp
- Triển khai tính idempotency
- Thêm xử lý lỗi
- Giám sát thời gian thực thi

### 3. **Hiệu suất**
- Sử dụng xử lý bất đồng bộ
- Triển khai các thao tác hàng loạt
- Cache dữ liệu truy cập thường xuyên
- Giám sát sử dụng tài nguyên

### 4. **Độ tin cậy**
- Triển khai cơ chế thử lại
- Sử dụng hàng đợi thư chết
- Ghi nhật ký tất cả hoạt động quan trọng
- Có chiến lược dự phòng

---

## 📊 Tổng kết Thống kê

### Tóm tắt các loại Event:
- **Tổng số loại Event**: 40+
- **Events Mượn sách**: 8 loại
- **Events Đơn hàng**: 5 loại
- **Events Thanh toán**: 4 loại
- **Events Người dùng**: 5 loại
- **Events Sách**: 4 loại
- **Events Hệ thống**: 4 loại

### Tóm tắt Scheduler:
- **Tổng số Tác vụ định kỳ**: 15+
- **Tác vụ Thông báo**: 5
- **Tác vụ Bảo trì**: 4
- **Tác vụ Phân tích**: 3
- **Tác vụ Tích hợp**: 3

### Khối lượng Event hàng ngày (Ước tính):
- **Khối lượng cao**: BookAccessedEvent (~5000/ngày)
- **Khối lượng trung bình**: NotificationCreatedEvent (~1000/ngày)
- **Khối lượng thấp**: SystemMaintenanceEvent (~1/ngày)

---

## 🚀 Kết luận

Kiến trúc hướng sự kiện kết hợp với Các tác vụ định kỳ tạo ra một hệ thống mạnh mẽ, có thể mở rộng và dễ bảo trì:

1. **Tự động hóa** các hoạt động thường ngày
2. **Mở rộng** theo chiều ngang một cách dễ dàng
3. **Duy trì** sự liên kết lỏng lẻo giữa các module
4. **Cung cấp** cập nhật thời gian thực
5. **Đảm bảo** độ tin cậy của hệ thống
6. **Cho phép** ra quyết định dựa trên dữ liệu

Kiến trúc này tạo thành xương sống của Hệ thống Quản lý Thư viện, cho phép nó xử lý các quy trình phức tạp một cách hiệu quả đồng thời duy trì chất lượng code và hiệu suất hệ thống.