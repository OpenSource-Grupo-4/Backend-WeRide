package org.example.backendweride.platform.profile.interfaces.rest.transform;

import org.example.backendweride.platform.profile.domain.model.commands.UpdateProfileCommand;
import org.example.backendweride.platform.profile.interfaces.rest.resources.UpdateProfileResource;

public class UpdateProfileCommandFromResourceAssembler {
    public static UpdateProfileCommand toCommand(Long profileId, UpdateProfileResource resource) {
        return new UpdateProfileCommand(
                profileId,
                resource.name(),
                resource.phone(),
                resource.profilePicture(),
                resource.dateOfBirth(),
                resource.address(),
                resource.emergencyContact()
        );
    }
}