package org.example.backendweride.platform.travelhistory.domain.services.queryservices;

import org.example.backendweride.platform.travelhistory.domain.model.queries.GetAllTravelsHistory;
import org.example.backendweride.platform.travelhistory.domain.model.queries.GetTravelsHistoryById;
import org.example.backendweride.platform.travelhistory.interfaces.resources.TravelHistoryResource;

import java.util.List;
import java.util.Optional;

public interface TravelHistoryQueryService {
    Optional<List<TravelHistoryResource>> handle(GetTravelsHistoryById query);
    Optional<List<TravelHistoryResource>> handle(GetAllTravelsHistory query);
}
