package org.example.backendweride.platform.profile.interfaces.transform;

import org.example.backendweride.platform.profile.domain.model.commands.CreateProfileCommand;
import org.example.backendweride.platform.profile.interfaces.resources.CreateProfileCommandResource;

public class CreateProfileCommandFromResourceAssembler {
    public static CreateProfileCommand toCommandFromResource(CreateProfileCommandResource resource) {
        return new CreateProfileCommand(
                resource.userId()
        );
    }
}
