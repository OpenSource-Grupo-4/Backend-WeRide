package com.weride.platform.garage.infrastructure.persistence.jpa.repositories;

import com.weride.platform.garage.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
