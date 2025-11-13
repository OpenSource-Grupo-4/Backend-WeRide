package org.example.backendweride.platform.booking.domain.model.commands;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command to save a booking draft.
 *
 * @summary This command encapsulates the data required to save a draft of a booking for a vehicle by a customer.
 */

public record SaveBookingDraftCommand(
    String draftId,
    String customerId,
    String vehicleId,
    LocalDate date,
    LocalTime unlockTime,
    int durationMinutes
) {
}
