package org.example.backendweride.platform.profile.interfaces.resources;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public record CreateProfileCommandResource(
        @NotNull Long userId,
        String firstName,
        String lastName,
        String email
) {
}
