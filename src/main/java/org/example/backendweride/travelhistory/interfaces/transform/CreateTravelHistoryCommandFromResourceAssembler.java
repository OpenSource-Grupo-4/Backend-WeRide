package org.example.backendweride.travelhistory.interfaces.transform;

import org.example.backendweride.travelhistory.domain.model.commands.CreateTravelHistoryCommand;
import org.example.backendweride.travelhistory.interfaces.resources.CreateTravelHistoryResource;

public class CreateTravelHistoryCommandFromResourceAssembler {
    public static CreateTravelHistoryCommand toCommandFronResource(CreateTravelHistoryResource travelHistoryResource){
        return new CreateTravelHistoryCommand(
                travelHistoryResource.userId(),
                travelHistoryResource.location(),
                travelHistoryResource.vehicle(),
                travelHistoryResource.image(),
                travelHistoryResource.tripDuration(),
                travelHistoryResource.travelDistance()
        );
    }
}
