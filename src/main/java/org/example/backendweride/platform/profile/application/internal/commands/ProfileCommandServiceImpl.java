package org.example.backendweride.platform.profile.application.internal.commands;

import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.example.backendweride.platform.profile.domain.model.commands.CreateProfileCommand;
import org.example.backendweride.platform.profile.domain.model.commands.UpdateProfileCommand;
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
        // Upsert: one profile per account
        return Optional.of(profileRepository.findByUserId(command.userId())
                .map(profile -> {
                    profile.update(command.firstName(), command.lastName(), command.email());
                    return profileRepository.save(profile);
                })
                .orElseGet(() -> profileRepository.save(new Profile(command))));
    }

    @Override
    public Optional<Profile> updateProfile(Long userId, UpdateProfileCommand command) {
        return profileRepository.findByUserId(userId)
                .map(profile -> {
                    profile.update(command.firstName(), command.lastName(), command.email());
                    return profileRepository.save(profile);
                });
    }
}
