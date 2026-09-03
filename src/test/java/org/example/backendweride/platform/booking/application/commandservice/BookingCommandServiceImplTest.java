package org.example.backendweride.platform.booking.application.commandservice;

import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;
import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingRepository;
import org.example.backendweride.platform.garage.domain.model.aggregates.Vehicle;
import org.example.backendweride.platform.garage.infrastructure.persistence.jpa.VehicleRepository;
import org.example.backendweride.platform.location.infrastructure.persistence.jpa.LocationRepository;
import org.example.backendweride.platform.notifications.interfaces.acl.NotificationContextFacade;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookingCommandServiceImplTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
    private final LocationRepository locationRepository = mock(LocationRepository.class);
    private final NotificationContextFacade notificationContextFacade = mock(NotificationContextFacade.class);
    private final BookingCommandServiceImpl service = new BookingCommandServiceImpl(
            bookingRepository, vehicleRepository, locationRepository, notificationContextFacade);

    private SaveBookingDraftCommand draftCommand(Long vehicleId) {
        return new SaveBookingDraftCommand(
                1L, vehicleId, 2L, 3L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                null, null,
                "draft", null, null, null,
                "credit_card", "pending",
                null, 30, null,
                null, null
        );
    }

    @Test
    void saveDraft_returnsFailure_whenVehicleNotInRealRepository() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        var result = service.saveDraft(draftCommand(999L));

        assertFalse(result.success());
        assertEquals("Vehicle not found", result.message());
    }

    @Test
    void saveDraft_succeeds_whenVehicleExistsInRealRepository() {
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getPricePerMinute()).thenReturn(0.5);
        when(vehicleRepository.findById(7L)).thenReturn(Optional.of(vehicle));
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.saveDraft(draftCommand(7L));

        assertTrue(result.success());
        assertEquals("Draft saved", result.message());
    }
}
