package com.weride.platform.garage.application.internal.commandservices;

import com.weride.platform.garage.domain.model.aggregates.User;
import com.weride.platform.garage.domain.model.commands.RegisterUserCommand;
import com.weride.platform.garage.infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUserCommandService {
    private final UserRepository userRepository;

    public User handle(RegisterUserCommand command) {
        User user = User.builder()
                .username(command.getUsername())
                .email(command.getEmail())
                .password(command.getPassword())
                .build();
        return userRepository.save(user);
    }
}
