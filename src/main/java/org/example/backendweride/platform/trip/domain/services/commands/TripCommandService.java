package org.example.backendweride.platform.trip.domain.services.commands;

import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.example.backendweride.platform.trip.domain.commands.CreateTripCommand;
import org.example.backendweride.platform.trip.domain.commands.UpdateTripCommand;

import java.util.Optional;

public interface TripCommandService {
    Optional<Trip> handle(CreateTripCommand command);
    Optional<Trip> handle(Long id, String userId, UpdateTripCommand command);
    boolean handle(Long id, String userId);
}
