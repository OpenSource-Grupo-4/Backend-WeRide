package org.example.backendweride.platform.profile.interfaces;

import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.example.backendweride.platform.profile.domain.services.commands.ProfileCommandService;
import org.example.backendweride.platform.profile.domain.services.queries.ProfileQueryService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileControllerTest {

    private final ProfileCommandService profileCommandService = mock(ProfileCommandService.class);
    private final ProfileQueryService profileQueryService = mock(ProfileQueryService.class);
    private final AuthenticatedAccountProvider authenticatedAccountProvider = mock(AuthenticatedAccountProvider.class);

    private final ProfileController controller = new ProfileController(
            profileCommandService, profileQueryService, authenticatedAccountProvider);

    @Test
    void getProfileById_returnsNotFound_whenProfileBelongsToAnotherUser() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        Profile foreign = mock(Profile.class);
        when(foreign.getUserId()).thenReturn(999L);
        when(profileQueryService.handle(3L)).thenReturn(Optional.of(foreign));

        var response = controller.getProfileById(3L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getProfileById_returnsProfile_whenOwnedByCurrentUser() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        Profile owned = mock(Profile.class);
        when(owned.getUserId()).thenReturn(42L);
        when(profileQueryService.handle(3L)).thenReturn(Optional.of(owned));

        var response = controller.getProfileById(3L);

        assertEquals(200, response.getStatusCode().value());
    }
}
