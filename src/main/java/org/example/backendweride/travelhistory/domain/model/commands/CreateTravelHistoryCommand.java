package org.example.backendweride.travelhistory.domain.model.commands;

public record CreateTravelHistoryCommand(
        Long userId,
        String location,
        String vehicle,
        String image,
        String tripDuration,
        String travelDistance
) {
}
