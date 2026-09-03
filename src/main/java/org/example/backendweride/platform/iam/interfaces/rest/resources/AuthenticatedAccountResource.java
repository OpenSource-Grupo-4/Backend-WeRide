package org.example.backendweride.platform.iam.interfaces.rest.resources;

/**
 * Authenticated Account Resource
 *
 * @summary Represents an authenticated account resource with its ID, token and linked profile id.
 */
public record AuthenticatedAccountResource(Long id, String token, Long profileId) {}
