package org.example.backendweride.platform.booking.domain.model.commands;

/**
 * Command to delete a booking draft.
 *
 * @summary This command encapsulates the data required to delete an existing booking draft.
 */
public record DeleteBookingDraftCommand(
    Long draftId,
    Long userId
) {
}

