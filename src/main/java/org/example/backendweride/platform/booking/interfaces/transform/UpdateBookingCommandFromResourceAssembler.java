package org.example.backendweride.platform.booking.interfaces.transform;

import org.example.backendweride.platform.booking.domain.model.commands.UpdateBookingCommand;
import org.example.backendweride.platform.booking.interfaces.resources.UpdateBookingResource;

public final class UpdateBookingCommandFromResourceAssembler {
    private UpdateBookingCommandFromResourceAssembler() {}

    public static UpdateBookingCommand toCommand(UpdateBookingResource resource) {
        var rating = resource.rating();
        return new UpdateBookingCommand(
                resource.vehicleId(), resource.startLocationId(), resource.endLocationId(),
                resource.reservedAt(), resource.startDate(), resource.endDate(),
                resource.actualStartDate(), resource.actualEndDate(), resource.status(),
                resource.totalCost(), resource.discount(), resource.finalCost(),
                resource.paymentMethod(), resource.paymentStatus(), resource.distance(),
                resource.duration(), resource.averageSpeed(),
                rating == null ? null : rating.score(), rating == null ? null : rating.comment());
    }
}
