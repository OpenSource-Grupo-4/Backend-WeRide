package org.example.backendweride.travelhistory.interfaces.transform;

import org.example.backendweride.travelhistory.domain.model.aggregates.TravelHistory;
import org.example.backendweride.travelhistory.interfaces.resources.TravelHistoryResource;

public class TravelHistoryResourceFromEntityAssembler {
    public static TravelHistoryResource toTravelHistoryFromEntity(TravelHistory entity) {
        return new TravelHistoryResource(
                entity.getId(),
                entity.getUserId(),
                entity.getLocation(),
                entity.getVehicle(),
                entity.getImage(),
                entity.getTripDuration(),
                entity.getTravelDistance(),
                entity.getCreatedAt()
        );
    }
}
