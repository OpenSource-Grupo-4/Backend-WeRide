package org.example.backendweride.platform.trip.application.internal.commands;

import org.example.backendweride.platform.notifications.interfaces.acl.NotificationContextFacade;
import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.example.backendweride.platform.trip.domain.commands.CreateTripCommand;
import org.example.backendweride.platform.trip.domain.commands.UpdateTripCommand;
import org.example.backendweride.platform.trip.domain.services.commands.TripCommandService;
import org.example.backendweride.platform.trip.infrastructure.persistence.jpa.TripRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TripCommandServiceImpl implements TripCommandService {
    private final TripRepository tripRepository;
    private final NotificationContextFacade notificationContextFacade;

    public TripCommandServiceImpl(TripRepository tripRepository,
                                  NotificationContextFacade notificationContextFacade) {
        this.tripRepository = tripRepository;
        this.notificationContextFacade = notificationContextFacade;
    }

    @Override
    public Optional<Trip> handle(CreateTripCommand command) {
        var trip = new Trip(command);
        var result = this.tripRepository.save(trip);

        try {
            Long accountId = Long.valueOf(command.userId());
            notificationContextFacade.notifyAccount(
                    accountId,
                    "Viaje registrado",
                    "Tu viaje fue registrado correctamente.",
                    "trip",
                    "confirmation",
                    String.valueOf(result.getId()),
                    "trip");
        } catch (NumberFormatException | NullPointerException ignored) {
            // userId is not a numeric account id, or the trip has no generated id yet —
            // nothing to notify
        }

        return Optional.of(result);
    }

    @Override
    public Optional<Trip> handle(Long id, String userId, UpdateTripCommand command) {
        return tripRepository.findById(id)
                .filter(trip -> userId.equals(trip.getUserId()))
                .map(trip -> {
                    trip.updateFrom(command);
                    return tripRepository.save(trip);
                });
    }

    @Override
    public boolean handle(Long id, String userId) {
        var trip = tripRepository.findById(id).filter(t -> userId.equals(t.getUserId()));
        trip.ifPresent(tripRepository::delete);
        return trip.isPresent();
    }
}
