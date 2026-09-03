package org.example.backendweride.platform.profile.interfaces.resources;

public record UpdateProfileResource(
        String firstName,
        String lastName,
        String email
) {
}
