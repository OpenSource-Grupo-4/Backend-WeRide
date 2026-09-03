package org.example.backendweride.platform.notifications.interfaces.transform;

import org.example.backendweride.platform.notifications.domain.model.commands.CreateNotificationCommand;
import org.example.backendweride.platform.notifications.interfaces.resources.CreateNotificationResource;

public class CreateNotificationCommandFromResourceAssembler {
    public static CreateNotificationCommand toCommandFromResource(CreateNotificationResource resource, String userId) {
        return new CreateNotificationCommand(
                userId,
                resource.title(),
                resource.message(),
                resource.type(),
                resource.category(),
                resource.priority(),
                resource.relatedEntityId(),
                resource.relatedEntityType(),
                resource.icon(),
                resource.color()
        );
    }
}
