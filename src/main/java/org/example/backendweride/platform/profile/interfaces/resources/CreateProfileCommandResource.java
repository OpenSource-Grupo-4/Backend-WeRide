package org.example.backendweride.platform.profile.interfaces.resources;

import lombok.Getter;

public record CreateProfileCommandResource(
        Long userId,
        String firstName,
        String lastName,
        String email
) {
}
