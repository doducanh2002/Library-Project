package com.library.event;

import com.library.entity.enums.NotificationType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class NotificationEvent extends ApplicationEvent {
    
    private final String userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final Long referenceId;
    private final String referenceType;
    private final Integer priority;
    private final LocalDateTime eventTime;

    public NotificationEvent(Object source, String userId, NotificationType type, 
                           String title, String message, Long referenceId, String referenceType, Integer priority) {
        super(source);
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.priority = priority;
        this.eventTime = LocalDateTime.now();
    }

    public NotificationEvent(Object source, String userId, NotificationType type, String title, String message) {
        this(source, userId, type, title, message, null, null, 1);
    }

    public NotificationEvent(Object source, String userId, NotificationType type, String title, String message, Integer priority) {
        this(source, userId, type, title, message, null, null, priority);
    }
}