package org.example.backendweride.platform.booking.domain.model.commands;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateBookingCommand(
    String bookingId,
    String customerId,
    String vehicleId,
    LocalDate date,
    LocalTime unlockTime,
    int durationMinutes
) {
}
