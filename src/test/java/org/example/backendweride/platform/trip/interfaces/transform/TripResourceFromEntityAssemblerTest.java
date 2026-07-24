package org.example.backendweride.platform.trip.interfaces.transform;

import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.example.backendweride.platform.trip.domain.commands.CreateTripCommand;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TripResourceFromEntityAssemblerTest {

    @Test
    void toResource_mapsCarbonSavedFromEntity_notMaxSpeed() {
        CreateTripCommand command = new CreateTripCommand(
                1L, "user-1", 2L, 3L, 4L,
                "route", List.of(),
                new Date(), new Date(),
                30, 5.5f, 20.0f, 45.0f, 10.0f,
                2.5f,
                100, "sunny", 22, "completed",
                List.of(), List.of()
        );
        Trip trip = new Trip(command);

        var resource = TripResourceFromEntityAssembler.toResource(trip);

        assertEquals(2.5f, resource.carbonSaved());
        assertEquals(45.0f, resource.maxSpeed());
    }
}
