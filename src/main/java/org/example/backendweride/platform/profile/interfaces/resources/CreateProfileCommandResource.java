package org.example.backendweride.platform.profile.interfaces.resources;

public record CreateProfileCommandResource(
        String firstName,
        String lastName,
        String email
) {
}
