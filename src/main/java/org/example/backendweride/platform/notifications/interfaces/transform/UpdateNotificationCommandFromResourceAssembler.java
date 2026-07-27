package org.example.backendweride.platform.notifications.interfaces.transform;

import org.example.backendweride.platform.notifications.domain.model.commands.UpdateNotificationCommand;
import org.example.backendweride.platform.notifications.interfaces.resources.UpdateNotificationResource;

public final class UpdateNotificationCommandFromResourceAssembler {
    private UpdateNotificationCommandFromResourceAssembler() {}

    public static UpdateNotificationCommand toCommand(UpdateNotificationResource resource) {
        return new UpdateNotificationCommand(
                resource.title(), resource.message(), resource.type(), resource.category(),
                resource.priority(), resource.isRead(), resource.actionRequired(),
                resource.relatedEntityId(), resource.relatedEntityType(), resource.icon(), resource.color());
    }
}
