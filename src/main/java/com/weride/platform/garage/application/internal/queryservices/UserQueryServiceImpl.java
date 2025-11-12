package com.weride.platform.garage.application.internal.queryservices;

import com.weride.platform.garage.domain.model.aggregates.User;
import com.weride.platform.garage.infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl {
    private final UserRepository repository;

    public List<User> handle() { return repository.findAll(); }
}
