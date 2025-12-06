package org.example.backendweride.platform.booking.domain.services;

import java.util.Optional;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.model.commands.DeleteBookingDraftCommand;

/**
 * Service interface for handling booking draft operations.
 *
 * @summary This service manages operations such as saving, updating, deleting, and retrieving booking drafts.
 */
public interface BookingDraftService {

    BookingCommandService.SaveDraftResult saveDraft(SaveBookingDraftCommand command);

    BookingCommandService.SaveDraftResult updateDraft(SaveBookingDraftCommand command);

    BookingCommandService.SaveDraftResult deleteDraft(DeleteBookingDraftCommand command);

    Optional<SaveBookingDraftCommand> getDraftById(Long draftId);

}
