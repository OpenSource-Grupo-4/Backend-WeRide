package org.example.backendweride.platform.profile.domain.services.queries;

import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;

import java.util.Optional;

public interface ProfileQueryService {
    Optional<Profile> handle(Long id);

}
