package org.example.backendweride.platform.booking.domain.services;

import org.example.backendweride.platform.booking.domain.model.commands.CreateBookingCommand;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;

public interface BookingCommandService {

    SaveDraftResult saveDraft(SaveBookingDraftCommand command);

    CreateBookingResult createBooking(CreateBookingCommand command);

    // Result types kept in domain layer for simplicity
    record SaveDraftResult(String draftId, boolean success, String message) { }

    record CreateBookingResult(String bookingId, boolean success, String message) { }
}
