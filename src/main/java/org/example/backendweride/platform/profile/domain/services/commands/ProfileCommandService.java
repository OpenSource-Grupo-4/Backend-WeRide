package org.example.backendweride.platform.profile.domain.services.commands;

import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.example.backendweride.platform.profile.domain.model.commands.CreateProfileCommand;
import org.example.backendweride.platform.profile.domain.model.commands.UpdateProfileCommand;

import java.util.Optional;

public interface ProfileCommandService {
    /**
     * Creates the profile for an account, or updates it when it already exists (upsert).
     * One profile per account is guaranteed by the uk_profile_user constraint.
     */
    Optional<Profile> handle(CreateProfileCommand command);

    /**
     * Updates the profile of the given user (owner-scoped). Returns empty when no profile exists.
     */
    Optional<Profile> updateProfile(Long userId, UpdateProfileCommand command);
}
