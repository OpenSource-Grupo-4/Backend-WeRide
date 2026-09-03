package org.example.backendweride.platform.travelhistory.interfaces.resources;

public record CreateTravelHistoryResource(
        String location,
        String vehicle,
        String image,
        String tripDuration,
        String travelDistance
) {
}
