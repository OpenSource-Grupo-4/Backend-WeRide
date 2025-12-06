package org.example.backendweride.platform.booking.domain.model.commands;

/**
 * Command to cancel a booking.
 *
 * @summary This command encapsulates the data required to cancel an existing booking.
 */
public record CancelBookingCommand(
    String bookingId,
    String customerId,
    String cancellationReason
) {
}

