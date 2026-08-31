package org.example.backendweride.platform.shared.infraestructure.persistence.jpa.strategy;

import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;
import org.example.backendweride.platform.booking.domain.model.entities.BookingIssue;
import org.example.backendweride.platform.garage.domain.model.aggregates.Vehicle;
import org.example.backendweride.platform.garage.domain.model.entities.VehicleFeature;
import org.example.backendweride.platform.iam.domain.model.aggregates.Account;
import org.example.backendweride.platform.location.domain.model.aggregates.Location;
import org.example.backendweride.platform.location.domain.model.entities.LocationAmenity;
import org.example.backendweride.platform.notifications.domain.model.aggregates.Notification;
import org.example.backendweride.platform.plan.domain.model.aggregates.Plan;
import org.example.backendweride.platform.plan.domain.model.entities.PlanBenefit;
import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.example.backendweride.platform.travelhistory.domain.model.aggregates.TravelHistory;
import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.example.backendweride.platform.trip.domain.entities.TripIncidentReport;
import org.example.backendweride.platform.trip.domain.entities.TripPhoto;
import org.example.backendweride.platform.trip.domain.entities.TripRouteCoordinate;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
                    .addAnnotatedClass(BookingIssue.class)
                    .addAnnotatedClass(Location.class)
                    .addAnnotatedClass(LocationAmenity.class)
                    .addAnnotatedClass(Notification.class)
                    .addAnnotatedClass(Plan.class)
                    .addAnnotatedClass(PlanBenefit.class)
                    .addAnnotatedClass(Profile.class)
                    .addAnnotatedClass(TravelHistory.class)
                    .addAnnotatedClass(Trip.class)
                    .addAnnotatedClass(TripIncidentReport.class)
                    .addAnnotatedClass(TripPhoto.class)
                    .addAnnotatedClass(TripRouteCoordinate.class)
                    .addAnnotatedClass(UnlockRequest.class)
                    .addAnnotatedClass(Vehicle.class)
                    .addAnnotatedClass(VehicleFeature.class)
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
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    void childCollectionTablesHaveAPrimaryKey() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, "org.hibernate.dialect.MySQLDialect")
                .applySetting(AvailableSettings.PHYSICAL_NAMING_STRATEGY,
                        SnakeCasePhysicalNamingStrategy.class.getName())
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", false)
                .build();

        try {
            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(Booking.class)
                    .addAnnotatedClass(BookingIssue.class)
                    .addAnnotatedClass(Location.class)
                    .addAnnotatedClass(LocationAmenity.class)
                    .addAnnotatedClass(Plan.class)
                    .addAnnotatedClass(PlanBenefit.class)
                    .addAnnotatedClass(Trip.class)
                    .addAnnotatedClass(TripIncidentReport.class)
                    .addAnnotatedClass(TripPhoto.class)
                    .addAnnotatedClass(TripRouteCoordinate.class)
                    .addAnnotatedClass(Vehicle.class)
                    .addAnnotatedClass(VehicleFeature.class)
                    .buildMetadata();

            String[] collectionTableNames = {
                    "vehicle_features",
                    "booking_issues",
                    "plan_benefits",
                    "location_amenities",
                    "trip_incident_reports",
                    "trip_photos",
                    "trip_route_coordinates"
            };

            for (String tableName : collectionTableNames) {
                org.hibernate.mapping.Table table = metadata.getDatabase().getDefaultNamespace().locateTable(
                        new org.hibernate.boot.model.naming.Identifier(tableName, false));
                assertNotNull(table, "Expected table " + tableName + " to be mapped");
                assertFalse(table.getPrimaryKey() == null || table.getPrimaryKey().getColumns().isEmpty(),
                        "Table " + tableName + " must declare a PRIMARY KEY (sql_require_primary_key=ON)");
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
