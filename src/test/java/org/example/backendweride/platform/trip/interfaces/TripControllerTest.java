package org.example.backendweride.platform.trip.interfaces;

import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
import org.example.backendweride.platform.trip.application.internal.commands.TripCommandServiceImpl;
import org.example.backendweride.platform.trip.domain.services.queries.TripQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripControllerTest {

    private final TripCommandServiceImpl tripCommandService = mock(TripCommandServiceImpl.class);
    private final TripQueryService tripQueryService = mock(TripQueryService.class);
    private final AuthenticatedAccountProvider authenticatedAccountProvider = mock(AuthenticatedAccountProvider.class);

    private final TripController controller = new TripController(tripCommandService, tripQueryService, authenticatedAccountProvider);

    @Test
    void getAllTrips_usesAuthenticatedUserId() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(tripQueryService.handle("42")).thenReturn(Optional.of(List.of()));

        controller.getAllTrips();

        verify(tripQueryService).handle("42");
    }

    @Test
    void getAllTrips_returnsTripResourceNotEntity() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(1L);
        when(tripQueryService.handle("1")).thenReturn(Optional.of(List.of()));

        var response = controller.getAllTrips();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteTripById_passesAuthenticatedUserId() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);

        controller.deleteTripById(7L);

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(tripCommandService).handle(org.mockito.ArgumentMatchers.eq(7L), userIdCaptor.capture());
        assertEquals("42", userIdCaptor.getValue());
    }
}
