package org.example.backendweride.travelhistory.interfaces.resources;

public record CreateTravelHistoryResource(
        Long userId,
        String location,
        String vehicle,
        String image,
        String tripDuration,
        String travelDistance
) {
}
