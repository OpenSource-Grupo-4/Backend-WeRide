package org.example.backendweride.travelhistory.domain.services.commandservices;

import org.example.backendweride.travelhistory.domain.model.aggregates.TravelHistory;
import org.example.backendweride.travelhistory.domain.model.commands.CreateTravelHistoryCommand;

import java.util.Optional;

public interface TravelHistoryCommandService {
    Optional<TravelHistory> handle(CreateTravelHistoryCommand travelHistoryCommand);
}
