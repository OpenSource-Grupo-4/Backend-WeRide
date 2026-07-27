package org.example.backendweride.platform.trip.domain.services.queries;

import org.example.backendweride.platform.trip.interfaces.resources.TripResource;

import java.util.List;
import java.util.Optional;

public interface TripQueryService {
    Optional<List<TripResource>> handle(String userId);
    Optional<TripResource> handle(Long id, String userId);
}
