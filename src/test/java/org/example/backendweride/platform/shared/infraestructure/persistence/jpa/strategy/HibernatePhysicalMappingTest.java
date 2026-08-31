package org.example.backendweride.platform.shared.infraestructure.persistence.jpa.strategy;

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
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.mapping.PersistentClass;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HibernatePhysicalMappingTest {

    @Test
    void resolvesEveryEntityAndCollectionToTheExpectedPhysicalTable() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, "org.hibernate.dialect.MySQLDialect")
                .applySetting(AvailableSettings.PHYSICAL_NAMING_STRATEGY,
                        SnakeCasePhysicalNamingStrategy.class.getName())
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", false)
                .build();

        try {
            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(Account.class)
                    .addAnnotatedClass(Booking.class)
                    .addAnnotatedClass(Location.class)
                    .addAnnotatedClass(Notification.class)
                    .addAnnotatedClass(Plan.class)
                    .addAnnotatedClass(Profile.class)
                    .addAnnotatedClass(TravelHistory.class)
                    .addAnnotatedClass(Trip.class)
                    .addAnnotatedClass(UnlockRequest.class)
                    .addAnnotatedClass(Vehicle.class)
                    .buildMetadata();

            Map<Class<?>, String> entityTables = Map.of(
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
            entityTables.forEach((entityClass, expectedTable) -> {
                PersistentClass binding = metadata.getEntityBinding(entityClass.getName());
                assertEquals(expectedTable, binding.getTable().getName());
            });

            Map<String, String> collectionTables = Map.of(
                    Booking.class.getName() + ".issues", "booking_issues",
                    Location.class.getName() + ".amenities", "location_amenities",
                    Plan.class.getName() + ".benefits", "plan_benefits",
                    Trip.class.getName() + ".incidentReports", "trip_incident_reports",
                    Trip.class.getName() + ".photos", "trip_photos",
                    Trip.class.getName() + ".routeCoordinates", "trip_route_coordinates",
                    Vehicle.class.getName() + ".features", "vehicle_features"
            );
            collectionTables.forEach((role, expectedTable) ->
                    assertEquals(expectedTable,
                            metadata.getCollectionBinding(role).getCollectionTable().getName()));
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
