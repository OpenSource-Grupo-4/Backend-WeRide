package org.example.backendweride.platform.profile.domain.services;

import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.example.backendweride.platform.profile.domain.model.queries.GetProfileByAccountIdQuery;
import org.example.backendweride.platform.profile.domain.model.queries.GetProfileByIdQuery;

import java.util.Optional;

public interface ProfileQueryService {
    Optional<Profile> handle(GetProfileByIdQuery query);
    Optional<Profile> handle(GetProfileByAccountIdQuery query);
}