package org.example.backendweride.platform.trip.interfaces;

import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
import org.example.backendweride.platform.trip.application.internal.commands.TripCommandServiceImpl;
import org.example.backendweride.platform.trip.domain.commands.CreateTripCommand;
import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.example.backendweride.platform.trip.domain.services.queries.TripQueryService;
import org.example.backendweride.platform.trip.interfaces.resources.CreateTripCommandResource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import java.util.Date;
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

    @Test
    void createTrip_usesAuthenticatedUserIdNotResourceBody() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        var resource = new CreateTripCommandResource(
                1L, "someone-else-id", 2L, 3L, 4L, "route", List.of(),
                new Date(), new Date(), 10, 5f, 20f, 25f, 0f, 0f, 0,
                "sunny", 20, "active", List.of(), List.of()
        );
        ArgumentCaptor<CreateTripCommand> captor = ArgumentCaptor.forClass(CreateTripCommand.class);
        when(tripCommandService.handle(captor.capture())).thenReturn(Optional.of(mock(Trip.class)));

        controller.createTrip(resource);

        assertEquals("42", captor.getValue().userId());
    }
}
