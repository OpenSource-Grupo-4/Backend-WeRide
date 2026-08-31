package org.example.backendweride.platform.shared.infraestructure.persistence.jpa.strategy;

import jakarta.persistence.Table;
import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;
import org.example.backendweride.platform.garage.domain.model.aggregates.Vehicle;
import org.example.backendweride.platform.iam.domain.model.aggregates.Account;
import org.example.backendweride.platform.location.domain.model.aggregates.Location;
import org.example.backendweride.platform.notifications.domain.model.aggregates.Notification;
import org.example.backendweride.platform.plan.domain.model.aggregates.Plan;
import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.example.backendweride.platform.travelhistory.domain.model.aggregates.TravelHistory;
import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.example.backendweride.platform.unlockrequest.domain.model.UnlockRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JpaTableNamingTest {

    @Test
    void entitiesDeclareStablePluralTableNames() {
        Map<Class<?>, String> expectedNames = Map.of(
                Account.class, "accounts",
                Booking.class, "bookings",
                Location.class, "locations",
                Notification.class, "notifications",
                Plan.class, "plans",
                Profile.class, "profiles",
                TravelHistory.class, "travel_histories",
                Trip.class, "trips",
                UnlockRequest.class, "unlock_requests",
                Vehicle.class, "vehicles"
        );

        expectedNames.forEach((entityClass, expectedName) ->
                assertEquals(expectedName, entityClass.getAnnotation(Table.class).name(),
                        () -> "Unexpected table name for " + entityClass.getSimpleName()));
    }

    @Test
    void childEntitiesDeclareTheExpectedCollectionTableNames() {
        Map<Class<?>, String> expectedNames = Map.of(
                org.example.backendweride.platform.garage.domain.model.entities.VehicleFeature.class, "vehicle_features",
                org.example.backendweride.platform.booking.domain.model.entities.BookingIssue.class, "booking_issues",
                org.example.backendweride.platform.plan.domain.model.entities.PlanBenefit.class, "plan_benefits",
                org.example.backendweride.platform.location.domain.model.entities.LocationAmenity.class, "location_amenities",
                org.example.backendweride.platform.trip.domain.entities.TripIncidentReport.class, "trip_incident_reports",
                org.example.backendweride.platform.trip.domain.entities.TripPhoto.class, "trip_photos",
                org.example.backendweride.platform.trip.domain.entities.TripRouteCoordinate.class, "trip_route_coordinates"
        );

        expectedNames.forEach((entityClass, expectedName) ->
                assertEquals(expectedName, entityClass.getAnnotation(Table.class).name(),
                        () -> "Unexpected table name for " + entityClass.getSimpleName()));
    }
}
