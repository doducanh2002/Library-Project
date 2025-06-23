package com.library.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class LoanEvent extends ApplicationEvent {
    
    public enum LoanEventType {
        LOAN_APPROVED,
        LOAN_REJECTED,
        LOAN_DUE_SOON,
        LOAN_OVERDUE,
        LOAN_RETURNED,
        LOAN_RENEWED
    }
    
    private final LoanEventType eventType;
    private final String userId;
    private final Long loanId;
    private final String bookTitle;
    private final LocalDateTime dueDate;
    private final String reason;
    private final Integer daysOverdue;
    private final LocalDateTime timestamp;

    public LoanEvent(Object source, LoanEventType eventType, String userId, Long loanId, String bookTitle) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.loanId = loanId;
        this.bookTitle = bookTitle;
        this.dueDate = null;
        this.reason = null;
        this.daysOverdue = null;
        this.timestamp = LocalDateTime.now();
    }

    public LoanEvent(Object source, LoanEventType eventType, String userId, Long loanId, String bookTitle, String reason) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.loanId = loanId;
        this.bookTitle = bookTitle;
        this.dueDate = null;
        this.reason = reason;
        this.daysOverdue = null;
        this.timestamp = LocalDateTime.now();
    }

    public LoanEvent(Object source, LoanEventType eventType, String userId, Long loanId, String bookTitle, LocalDateTime dueDate) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.loanId = loanId;
        this.bookTitle = bookTitle;
        this.dueDate = dueDate;
        this.reason = null;
        this.daysOverdue = null;
        this.timestamp = LocalDateTime.now();
    }

    public LoanEvent(Object source, LoanEventType eventType, String userId, Long loanId, String bookTitle, Integer daysOverdue) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.loanId = loanId;
        this.bookTitle = bookTitle;
        this.dueDate = null;
        this.reason = null;
        this.daysOverdue = daysOverdue;
        this.timestamp = LocalDateTime.now();
    }
}