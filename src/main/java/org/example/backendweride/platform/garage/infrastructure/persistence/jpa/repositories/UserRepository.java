package org.example.backendweride.platform.garage.infrastructure.persistence.jpa.repositories;

import org.example.backendweride.platform.garage.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
