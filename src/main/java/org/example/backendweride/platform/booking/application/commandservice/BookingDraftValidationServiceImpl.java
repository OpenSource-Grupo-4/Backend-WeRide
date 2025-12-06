package org.example.backendweride.platform.booking.application.commandservice;

import org.springframework.stereotype.Service;

import org.example.backendweride.platform.booking.domain.services.BookingDraftValidationService;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;

import java.time.LocalDateTime;

/**
 * Implementation of BookingDraftValidationService to validate booking draft operations.
 *
 * @summary This service validates booking draft data before saving or updating.
 */
@Service
public class BookingDraftValidationServiceImpl implements BookingDraftValidationService {

    @Override
    public ValidationResult validateForSave(SaveBookingDraftCommand command) {
        if (command == null) {
            return new ValidationResult(false, "Command cannot be null");
        }

        if (command.userId() == null) {
            return new ValidationResult(false, "User ID is required");
        }

        if (command.vehicleId() == null) {
            return new ValidationResult(false, "Vehicle ID is required");
        }

        if (command.startDate() == null) {
            return new ValidationResult(false, "Start date is required");
        }

        if (command.startDate().isBefore(LocalDateTime.now())) {
            return new ValidationResult(false, "Start date cannot be in the past");
        }

        if (command.duration() == null || command.duration() <= 0) {
            return new ValidationResult(false, "Duration must be greater than 0");
        }

        if (command.duration() > 1440) { // Max 24 hours
            return new ValidationResult(false, "Duration cannot exceed 24 hours");
        }

        return new ValidationResult(true, "Validation successful");
    }
}
