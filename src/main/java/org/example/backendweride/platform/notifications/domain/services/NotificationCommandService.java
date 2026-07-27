package org.example.backendweride.platform.notifications.domain.services;

import org.example.backendweride.platform.notifications.domain.model.aggregates.Notification;
import org.example.backendweride.platform.notifications.domain.model.commands.CreateNotificationCommand;
import org.example.backendweride.platform.notifications.domain.model.commands.MarkNotificationAsReadCommand;
import org.example.backendweride.platform.notifications.domain.model.commands.UpdateNotificationCommand;

import java.util.Optional;

public interface NotificationCommandService {
    Long handle(CreateNotificationCommand command);
    void handle(MarkNotificationAsReadCommand command);
    Optional<Notification> update(String publicId, String userId, UpdateNotificationCommand command);
    boolean delete(String publicId, String userId);
}
