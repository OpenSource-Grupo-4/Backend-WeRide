package org.example.backendweride.platform.trip.application.internal.commands;

import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.example.backendweride.platform.trip.infrastructure.persistence.jpa.TripRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

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
}
