package org.example.backendweride.platform.profile.domain.services.commands;

import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.example.backendweride.platform.profile.domain.model.commands.CreateProfileCommand;

import java.util.Optional;

public interface ProfileCommandService {
    Optional<Profile> handle(CreateProfileCommand command);
}
