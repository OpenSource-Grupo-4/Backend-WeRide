package org.example.backendweride.platform.travelhistory.application.internal.queryservices;

import org.example.backendweride.platform.travelhistory.domain.model.queries.GetAllTravelsHistory;
import org.example.backendweride.platform.travelhistory.domain.model.queries.GetTravelsHistoryById;
import org.example.backendweride.platform.travelhistory.domain.services.queryservices.TravelHistoryQueryService;
import org.example.backendweride.platform.travelhistory.infrastructure.persistence.jpa.TravelHistoryRepository;
import org.example.backendweride.platform.travelhistory.interfaces.resources.TravelHistoryResource;
import org.example.backendweride.platform.travelhistory.interfaces.transform.TravelHistoryResourceFromEntityAssembler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TravelHistoryQueryServiceImpl implements the TravelHistoryQueryService interface
 * to handle queries related to travel history.
 *
 * @summary This service provides methods to retrieve travel history records by user ID
 *          and to retrieve all travel history records.
 */
@Service
public class TravelHistoryQueryServiceImpl implements TravelHistoryQueryService {

    private final TravelHistoryRepository travelHistoryRepository;
    public TravelHistoryQueryServiceImpl(TravelHistoryRepository travelHistoryRepository) {
        this.travelHistoryRepository = travelHistoryRepository;
    }

    @Override
    public Optional<List<TravelHistoryResource>> handle(GetTravelsHistoryById query) {
        var listTravelHistory = travelHistoryRepository.findByUserId(query.id()).stream()
                .map(TravelHistoryResourceFromEntityAssembler::toTravelHistoryFromEntity)
                .collect(Collectors.toList());
        return Optional.of(listTravelHistory);
    }

    @Override
    public Optional<List<TravelHistoryResource>> handle(GetAllTravelsHistory query) {
        var travelHistories = travelHistoryRepository.findAll().stream()
                .map(TravelHistoryResourceFromEntityAssembler::toTravelHistoryFromEntity)
                .collect(Collectors.toList());
        return Optional.of(travelHistories);
    }
}
