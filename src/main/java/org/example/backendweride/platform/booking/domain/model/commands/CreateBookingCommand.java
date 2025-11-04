package org.example.backendweride.platform.booking.domain.model.commands;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command to create a new booking.
 *
 * @summary This command encapsulates the data required to create a booking for a vehicle by a customer.
 */

public record CreateBookingCommand(
    String bookingId,
    String customerId,
    String vehicleId,
    LocalDate date,
    LocalTime unlockTime,
    int durationMinutes
) {
}
