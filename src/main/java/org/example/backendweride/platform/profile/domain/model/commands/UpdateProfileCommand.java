package org.example.backendweride.platform.profile.domain.model.commands;

public record UpdateProfileCommand(
        String firstName,
        String lastName,
        String email
) {
}
