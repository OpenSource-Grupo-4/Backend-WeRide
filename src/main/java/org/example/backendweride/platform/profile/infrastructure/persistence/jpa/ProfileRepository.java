package org.example.backendweride.platform.profile.infrastructure.persistence.jpa;

import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

}
