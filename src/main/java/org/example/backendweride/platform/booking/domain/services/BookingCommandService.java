package org.example.backendweride.platform.booking.domain.services;

import org.example.backendweride.platform.booking.domain.model.commands.CreateBookingCommand;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.model.commands.UpdateBookingCommand;

import java.util.Optional;
import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;

/**
 * Service interface for handling booking-related commands.
 *
 * @summary This service manages operations such as saving booking drafts and creating bookings.
 */
public interface BookingCommandService {

    SaveDraftResult saveDraft(SaveBookingDraftCommand command);

    CreateBookingResult createBooking(CreateBookingCommand command);

    Optional<Booking> updateBooking(Long id, Long userId, UpdateBookingCommand command);

    boolean deleteBooking(Long id, Long userId);

    // Result types kept in domain layer for simplicity
    record SaveDraftResult(Long draftId, boolean success, String message) { }

    record CreateBookingResult(Long bookingId, boolean success, String message) { }
}
