package org.example.backendweride.platform.profile.interfaces.transform;

import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.example.backendweride.platform.profile.interfaces.resources.ProfileResource;

public record ProfileResourceFromEntity() {

    public static ProfileResource tpProfileResourceFromEntity(Profile entity) {
        return new ProfileResource(
                entity.getId(),
                entity.getUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail()
        );
    }
}
