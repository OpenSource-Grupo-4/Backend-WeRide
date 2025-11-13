package org.example.backendweride.platform.booking.domain.services;

import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;

/**
 * Service interface for validating booking draft operations.
 *
 * @summary This service validates booking draft data before saving.
 */
public interface BookingDraftValidationService {

    ValidationResult validateForSave(SaveBookingDraftCommand command);

    record ValidationResult(boolean valid, String message) { }

}
