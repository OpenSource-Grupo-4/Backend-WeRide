package org.example.backendweride.platform.travelhistory.application.internal.queryservices;

import org.example.backendweride.platform.travelhistory.domain.model.aggregates.TravelHistory;
import org.example.backendweride.platform.travelhistory.domain.model.queries.GetTravelsHistoryById;
import org.example.backendweride.platform.travelhistory.domain.services.queryservices.TravelHistoryQueryService;
import org.example.backendweride.platform.travelhistory.infrastructure.persistence.jpa.TravelHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TravelHistoryQueryServiceImpl implements TravelHistoryQueryService {

    private final TravelHistoryRepository travelHistoryRepository;
    public TravelHistoryQueryServiceImpl(TravelHistoryRepository travelHistoryRepository) {
        this.travelHistoryRepository = travelHistoryRepository;
    }

    @Override
    public Optional<List<TravelHistory>> handle(GetTravelsHistoryById query) {
        var listTravelHistory = travelHistoryRepository.findByUserId(query.id());
        return Optional.of(listTravelHistory);
    }
}
