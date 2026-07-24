package org.example.backendweride.platform.location.application.internal.queryservices;

import org.example.backendweride.platform.location.domain.services.queryservices.LocationQueryService;
import org.example.backendweride.platform.location.infrastructure.persistence.jpa.LocationRepository;
import org.example.backendweride.platform.location.interfaces.resources.LocationResource;
import org.example.backendweride.platform.location.interfaces.transform.LocationResourceFromEntityAssembler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LocationQueryServiceImpl implements LocationQueryService {

    private final LocationRepository locationRepository;

    public LocationQueryServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public Optional<List<LocationResource>> handle() {
        var locations = this.locationRepository.findAll().stream()
                .map(LocationResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return Optional.of(locations);
    }
}
