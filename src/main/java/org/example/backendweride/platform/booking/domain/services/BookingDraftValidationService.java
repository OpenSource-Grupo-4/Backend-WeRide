package org.example.backendweride.platform.booking.domain.services;

import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;

public interface BookingDraftValidationService {

    ValidationResult validateForSave(SaveBookingDraftCommand command);

    record ValidationResult(boolean valid, String message) { }

}
