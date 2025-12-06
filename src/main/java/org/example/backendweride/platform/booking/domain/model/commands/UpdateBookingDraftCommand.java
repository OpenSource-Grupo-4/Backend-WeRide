package org.example.backendweride.platform.booking.domain.model.commands;

/**
 * Command to update a booking draft.
 *
 * @summary This command encapsulates the data required to update an existing booking draft.
 */
public record UpdateBookingDraftCommand(
    String draftId,
    String customerId,
    String vehicleId,
    java.time.LocalDate date,
    java.time.LocalTime unlockTime,
    int durationMinutes
) {
}

