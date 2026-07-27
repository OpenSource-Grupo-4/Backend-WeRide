package org.example.backendweride.platform.notifications.domain.model.commands;

public record UpdateNotificationCommand(
        String title,
        String message,
        String type,
        String category,
        String priority,
        Boolean isRead,
        Boolean actionRequired,
        String relatedEntityId,
        String relatedEntityType,
        String icon,
        String color
) {}
