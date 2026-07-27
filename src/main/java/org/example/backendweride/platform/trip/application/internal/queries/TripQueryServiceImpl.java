package org.example.backendweride.platform.trip.application.internal.queries;

import org.example.backendweride.platform.trip.domain.services.queries.TripQueryService;
import org.example.backendweride.platform.trip.infrastructure.persistence.jpa.TripRepository;
import org.example.backendweride.platform.trip.interfaces.resources.TripResource;
import org.example.backendweride.platform.trip.interfaces.transform.TripResourceFromEntityAssembler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TripQueryServiceImpl implements TripQueryService {
    private final TripRepository tripRepository;

    public TripQueryServiceImpl(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Override
    public Optional<List<TripResource>> handle(String userId) {
        var trips = tripRepository.findByUserId(userId).stream()
                .map(TripResourceFromEntityAssembler::toResource)
                .collect(Collectors.toList());
        return Optional.of(trips);
    }

    @Override
    public Optional<TripResource> handle(Long id, String userId) {
        return tripRepository.findById(id)
                .filter(trip -> userId.equals(trip.getUserId()))
                .map(TripResourceFromEntityAssembler::toResource);
    }
}
