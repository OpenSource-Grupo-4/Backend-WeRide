package org.example.backendweride.platform.shared.infraestructure.persistence.jpa.strategy;

import jakarta.persistence.CollectionTable;
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

import java.lang.reflect.Field;
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
    void vehicleFeaturesUsesTheExpectedCollectionTable() throws NoSuchFieldException {
        assertCollectionTable(Vehicle.class, "features", "vehicle_features");
    }

    @Test
    void tripRouteCoordinatesUsesAnExplicitCollectionTable() throws NoSuchFieldException {
        assertCollectionTable(Trip.class, "routeCoordinates", "trip_route_coordinates");
    }

    private void assertCollectionTable(Class<?> entityClass, String fieldName, String expectedName)
            throws NoSuchFieldException {
        Field field = entityClass.getDeclaredField(fieldName);

        assertEquals(expectedName, field.getAnnotation(CollectionTable.class).name());
    }
}
