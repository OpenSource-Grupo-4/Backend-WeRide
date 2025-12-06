package org.example.backendweride.platform.booking.domain.model.commands;

/**
 * Command to confirm a booking from a draft.
 *
 * @summary This command converts a booking draft into a confirmed booking.
 */
public record ConfirmBookingFromDraftCommand(
    String draftId,
    String customerId
) {
}

