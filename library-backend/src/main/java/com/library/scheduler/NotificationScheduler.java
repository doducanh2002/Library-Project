package com.library.scheduler;

import com.library.entity.Loan;
import com.library.entity.LoanStatus;
import com.library.event.LoanEvent;
import com.library.repository.LoanRepository;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final LoanRepository loanRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Check for loans due within next 24 hours and send due soon notifications
     * Runs every 6 hours
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 hours in milliseconds
    public void checkLoansDueSoon() {
        log.info("Checking for loans due soon...");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrow = now.plusDays(1);
            
            // Find active loans due within next 24 hours
            List<Loan> loansDueSoon = loanRepository.findLoansApproachingDueDate(
                    now, tomorrow, LoanStatus.BORROWED);
            
            log.info("Found {} loans due within next 24 hours", loansDueSoon.size());
            
            for (Loan loan : loansDueSoon) {
                try {
                    // Check if we already sent a due soon notification today
                    boolean alreadyNotified = hasRecentDueSoonNotification(loan);
                    
                    if (!alreadyNotified) {
                        LoanEvent event = new LoanEvent(
                                this,
                                LoanEvent.LoanEventType.LOAN_DUE_SOON,
                                loan.getUserId().toString(),
                                loan.getId(),
                                loan.getBook().getTitle(),
                                loan.getDueDate()
                        );
                        eventPublisher.publishEvent(event);
                        log.info("Published due soon notification for loan: {}", loan.getId());
                    }
                } catch (Exception e) {
                    log.error("Failed to process due soon notification for loan: {}", loan.getId(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to check loans due soon", e);
        }
    }

    /**
     * Check for overdue loans and send overdue notifications
     * Runs every 2 hours
     */
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000) // 2 hours in milliseconds
    public void checkOverdueLoans() {
        log.info("Checking for overdue loans...");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Find active loans that are overdue
            List<Loan> overdueLoans = loanRepository.findOverdueLoans(
                    LoanStatus.BORROWED, now);
            
            log.info("Found {} overdue loans", overdueLoans.size());
            
            for (Loan loan : overdueLoans) {
                try {
                    int daysOverdue = (int) ChronoUnit.DAYS.between(loan.getDueDate(), now);
                    
                    // Send daily notifications for first week, then weekly
                    boolean shouldNotify = shouldSendOverdueNotification(loan, daysOverdue);
                    
                    if (shouldNotify) {
                        LoanEvent event = new LoanEvent(
                                this,
                                LoanEvent.LoanEventType.LOAN_OVERDUE,
                                loan.getUserId().toString(),
                                loan.getId(),
                                loan.getBook().getTitle(),
                                Integer.valueOf(daysOverdue)
                        );
                        eventPublisher.publishEvent(event);
                        log.info("Published overdue notification for loan: {} ({} days overdue)", 
                                loan.getId(), daysOverdue);
                    }
                } catch (Exception e) {
                    log.error("Failed to process overdue notification for loan: {}", loan.getId(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to check overdue loans", e);
        }
    }

    /**
     * Cleanup expired and old notifications
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void cleanupNotifications() {
        log.info("Running notification cleanup...");
        
        try {
            // Archive expired notifications
            notificationService.cleanupExpiredNotifications();
            
            // Delete notifications older than 90 days (except unread ones)
            notificationService.cleanupOldNotifications(90);
            
            log.info("Notification cleanup completed");
        } catch (Exception e) {
            log.error("Failed to cleanup notifications", e);
        }
    }

    /**
     * Weekly system health notification for admins
     * Runs every Sunday at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * SUN") // Every Sunday at 9 AM
    public void sendWeeklySystemReport() {
        log.info("Sending weekly system report...");
        
        try {
            // This would typically get system statistics and send to admins
            // For now, just log the action
            log.info("Weekly system report would be sent to administrators");
            
            // In a real implementation, you would:
            // 1. Gather system statistics
            // 2. Create notification for admin users
            // 3. Send email reports if configured
            
        } catch (Exception e) {
            log.error("Failed to send weekly system report", e);
        }
    }

    private boolean hasRecentDueSoonNotification(Loan loan) {
        // Check if we sent a due soon notification in the last 12 hours
        // This would require checking notification history
        // For simplicity, we'll return false for now
        return false;
    }

    private boolean shouldSendOverdueNotification(Loan loan, int daysOverdue) {
        // Send notifications:
        // - Daily for first 7 days
        // - Weekly after that (on days 14, 21, 28, etc.)
        
        if (daysOverdue <= 7) {
            return true; // Send daily
        } else {
            return daysOverdue % 7 == 0; // Send weekly
        }
    }
}