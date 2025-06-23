package com.library.mapper;

import com.library.dto.CreateNotificationRequestDTO;
import com.library.dto.NotificationDTO;
import com.library.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "isUnread", expression = "java(notification.isUnread())")
    @Mapping(target = "isRead", expression = "java(notification.isRead())")
    @Mapping(target = "isExpired", expression = "java(notification.isExpired())")
    @Mapping(target = "isHighPriority", expression = "java(notification.isHighPriority())")
    @Mapping(target = "priorityLabel", expression = "java(notification.getPriorityLabel())")
    NotificationDTO toDTO(Notification notification);

    List<NotificationDTO> toDTOList(List<Notification> notifications);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailSentAt", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "sendEmail", target = "isEmailSent")
    Notification toEntity(CreateNotificationRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isEmailSent", ignore = true)
    @Mapping(target = "emailSentAt", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateNotificationFromDTO(CreateNotificationRequestDTO dto, @MappingTarget Notification notification);
}