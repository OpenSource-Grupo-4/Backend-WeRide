package org.example.backendweride.platform.trip.application.internal.commands;

import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.example.backendweride.platform.trip.infrastructure.persistence.jpa.TripRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TripCommandServiceImplTest {

    private final TripRepository tripRepository = mock(TripRepository.class);
    private final TripCommandServiceImpl service = new TripCommandServiceImpl(tripRepository);

    @Test
    void deleteById_doesNothing_whenTripBelongsToAnotherUser() {
        Trip foreign = mock(Trip.class);
        when(foreign.getUserId()).thenReturn("999");
        when(tripRepository.findById(7L)).thenReturn(Optional.of(foreign));

        service.handle(7L, "42");

        verify(tripRepository, never()).delete(any());
    }

    @Test
    void deleteById_deletes_whenOwnedByCurrentUser() {
        Trip owned = mock(Trip.class);
        when(owned.getUserId()).thenReturn("42");
        when(tripRepository.findById(7L)).thenReturn(Optional.of(owned));

        service.handle(7L, "42");

        verify(tripRepository).delete(owned);
    }

    @Test
    void handle_returnsFalseWhenTripDoesNotBelongToUser() {
        var trip = mock(Trip.class);
        when(trip.getUserId()).thenReturn("other-user");
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        boolean deleted = service.handle(1L, "current-user");

        assertFalse(deleted);
        verify(tripRepository, never()).delete(any());
    }

    @Test
    void handle_returnsTrueWhenTripBelongsToUser() {
        var trip = mock(Trip.class);
        when(trip.getUserId()).thenReturn("current-user");
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        boolean deleted = service.handle(1L, "current-user");

        assertTrue(deleted);
        verify(tripRepository).delete(trip);
    }
}
