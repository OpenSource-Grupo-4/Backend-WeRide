package org.example.backendweride.platform.profile.application.internal.queries;

import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.example.backendweride.platform.profile.domain.services.queries.ProfileQueryService;
import org.example.backendweride.platform.profile.infrastructure.persistence.jpa.ProfileRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProfileQueryServiceImpl implements ProfileQueryService {
    private final ProfileRepository profileRepository;

    public ProfileQueryServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public Optional<Profile> handle(Long id) {
        return  profileRepository.findById(id);
    }


}
