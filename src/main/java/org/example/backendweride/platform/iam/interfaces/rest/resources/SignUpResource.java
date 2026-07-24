package org.example.backendweride.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * Sign Up Resource
 *
 * @summary Represents the data required for signing up, including username and password.
 */
public record SignUpResource(@NotBlank String username, @NotBlank String password) {}
