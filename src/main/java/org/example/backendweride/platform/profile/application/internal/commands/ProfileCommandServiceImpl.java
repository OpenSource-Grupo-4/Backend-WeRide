package org.example.backendweride.platform.profile.application.internal.commands;

import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.example.backendweride.platform.profile.domain.model.commands.CreateProfileCommand;
import org.example.backendweride.platform.profile.domain.services.commands.ProfileCommandService;
import org.example.backendweride.platform.profile.infrastructure.persistence.jpa.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileCommandServiceImpl implements ProfileCommandService {
    private final ProfileRepository profileRepository;

    public ProfileCommandServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }


    @Override
    public Optional<Profile> handle(CreateProfileCommand command) {
        var profile = new Profile(command);
        var result = profileRepository.save(profile);
        return Optional.of(result);
    }
}
