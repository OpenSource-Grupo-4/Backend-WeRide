package org.example.backendweride.platform.profile.interfaces.resources;

public record ProfileResource(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String email
) {
}
