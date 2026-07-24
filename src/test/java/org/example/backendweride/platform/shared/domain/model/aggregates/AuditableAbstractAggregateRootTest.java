package org.example.backendweride.platform.shared.domain.model.aggregates;

import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class AuditableAbstractAggregateRootTest {

    @Test
    void entitiesWithSameIdAreEqual() throws Exception {
        Booking a = new Booking();
        Booking b = new Booking();
        setId(a, 1L);
        setId(b, 1L);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void entitiesWithDifferentIdAreNotEqual() throws Exception {
        Booking a = new Booking();
        Booking b = new Booking();
        setId(a, 1L);
        setId(b, 2L);
        assertNotEquals(a, b);
    }

    @Test
    void entitiesWithNullIdAreNeverEqualToAnother() {
        Booking a = new Booking();
        Booking b = new Booking();
        assertNotEquals(a, b);
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = AuditableAbstractAggregateRoot.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
