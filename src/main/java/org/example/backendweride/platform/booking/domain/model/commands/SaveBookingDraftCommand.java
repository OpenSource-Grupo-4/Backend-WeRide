package org.example.backendweride.platform.booking.domain.model.commands;

import java.time.LocalDate;
import java.time.LocalTime;

public record SaveBookingDraftCommand(
    String draftId,
    String customerId,
    String vehicleId,
    LocalDate date,
    LocalTime unlockTime,
    int durationMinutes
) {
}
