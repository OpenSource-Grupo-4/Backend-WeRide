package org.example.backendweride.platform.garage.application.internal.commandservices;

import org.example.backendweride.platform.garage.domain.model.aggregates.User;
import org.example.backendweride.platform.garage.domain.model.commands.RegisterUserCommand;
import org.example.backendweride.platform.garage.infrastructure.persistence.jpa.repositories.UserRepository;
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
