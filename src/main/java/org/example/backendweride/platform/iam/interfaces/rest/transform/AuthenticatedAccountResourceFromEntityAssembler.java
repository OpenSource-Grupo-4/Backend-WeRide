package org.example.backendweride.platform.iam.interfaces.rest.transform;

import org.example.backendweride.platform.iam.domain.model.aggregates.Account;
import org.example.backendweride.platform.iam.interfaces.rest.resources.AuthenticatedAccountResource;

/**
 * Assembler class to convert Account entity to AuthenticatedAccountResource.
 *
 * @summary This class provides a method to transform an Account entity
 *          into an AuthenticatedAccountResource for REST API responses.
 */
public class AuthenticatedAccountResourceFromEntityAssembler {
    public static AuthenticatedAccountResource toResourceFromEntity(Account entity, String token) {
        Long profileId = entity.getProfileId() != null ? entity.getProfileId().profileId() : null;
        return new AuthenticatedAccountResource(entity.getId(), token, profileId);
    }
}
