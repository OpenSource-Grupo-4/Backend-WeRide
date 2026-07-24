package org.example.backendweride.platform.profile.domain.model.commands;

public record CreateProfileCommand(
        Long userId,
        String firstName,
        String lastName,
        String email
) {
}
