package org.example.backendweride.platform.notifications.interfaces.acl;

import org.example.backendweride.platform.notifications.domain.model.commands.CreateNotificationCommand;
import org.example.backendweride.platform.notifications.domain.services.NotificationCommandService;
import org.springframework.stereotype.Service;

/**
 * NotificationContextFacade
 * Facade that lets other bounded contexts (booking, trip, ...) create notifications
 * without depending on the notification domain internals.
 */
@Service
public class NotificationContextFacade {

    private final NotificationCommandService notificationCommandService;

    public NotificationContextFacade(NotificationCommandService notificationCommandService) {
        this.notificationCommandService = notificationCommandService;
    }

    public void notifyAccount(
            Long userId,
            String title,
            String message,
            String type,
            String category,
            String relatedEntityId,
            String relatedEntityType) {
        var command = new CreateNotificationCommand(
                String.valueOf(userId),
                title,
                message,
                type,
                category,
                null,
                relatedEntityId,
                relatedEntityType,
                null,
                null);
        notificationCommandService.handle(command);
    }
}
