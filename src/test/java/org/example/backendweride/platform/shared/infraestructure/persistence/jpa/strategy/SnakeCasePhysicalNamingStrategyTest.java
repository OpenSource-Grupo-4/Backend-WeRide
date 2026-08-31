package org.example.backendweride.platform.shared.infraestructure.persistence.jpa.strategy;

import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnakeCasePhysicalNamingStrategyTest {

    private final SnakeCasePhysicalNamingStrategy strategy = new SnakeCasePhysicalNamingStrategy();

    @Test
    void convertsTableNamesToSnakeCaseWithoutPluralizingThem() {
        assertTableName("vehicles", "vehicles");
        assertTableName("vehicle_features", "vehicle_features");
        assertTableName("travelHistory", "travel_history");
        assertTableName("Booking", "booking");
    }

    @Test
    void convertsColumnNamesToSnakeCase() {
        assertColumnName("licensePlate", "license_plate");
        assertColumnName("companyId", "company_id");
        assertColumnName("pricePerMinute", "price_per_minute");
        assertColumnName("maintenanceStatus", "maintenance_status");
        assertColumnName("lastMaintenance", "last_maintenance");
        assertColumnName("nextMaintenance", "next_maintenance");
        assertColumnName("totalKilometers", "total_kilometers");
    }

    @Test
    void preservesQuotedIdentifiers() {
        Identifier physicalName = strategy.toPhysicalTableName(
                Identifier.toIdentifier("VehicleFeatures", true), null);

        assertEquals("vehicle_features", physicalName.getText());
        assertTrue(physicalName.isQuoted());
    }

    private void assertTableName(String logicalName, String expectedName) {
        Identifier physicalName = strategy.toPhysicalTableName(
                Identifier.toIdentifier(logicalName), null);

        assertEquals(expectedName, physicalName.getText());
    }

    private void assertColumnName(String logicalName, String expectedName) {
        Identifier physicalName = strategy.toPhysicalColumnName(
                Identifier.toIdentifier(logicalName), null);

        assertEquals(expectedName, physicalName.getText());
    }
}
