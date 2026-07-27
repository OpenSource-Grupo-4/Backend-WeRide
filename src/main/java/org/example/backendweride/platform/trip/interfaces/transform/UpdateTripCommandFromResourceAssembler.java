package org.example.backendweride.platform.trip.interfaces.transform;

import org.example.backendweride.platform.trip.domain.commands.UpdateTripCommand;
import org.example.backendweride.platform.trip.interfaces.resources.UpdateTripResource;

public final class UpdateTripCommandFromResourceAssembler {
    private UpdateTripCommandFromResourceAssembler() {}

    public static UpdateTripCommand toCommand(UpdateTripResource resource) {
        return new UpdateTripCommand(
                resource.bookingId(), resource.vehicleId(), resource.startLocationId(), resource.endLocationId(),
                resource.route(), resource.routeCoordinates(), resource.startDate(), resource.endDate(),
                resource.duration(), resource.distance(), resource.averageSpeed(), resource.maxSpeed(),
                resource.totalCost(), resource.carbonSaved(), resource.caloriesBurned(), resource.weather(),
                resource.temperature(), resource.status(), resource.incidentReports(), resource.photos());
    }
}
