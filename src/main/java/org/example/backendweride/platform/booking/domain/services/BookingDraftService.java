package org.example.backendweride.platform.booking.domain.services;

import java.util.Optional;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;

public interface BookingDraftService {

    BookingCommandService.SaveDraftResult saveDraft(SaveBookingDraftCommand command);

    BookingCommandService.SaveDraftResult updateDraft(SaveBookingDraftCommand command);

    BookingCommandService.SaveDraftResult deleteDraft(String draftId);

    Optional<SaveBookingDraftCommand> getDraftById(String draftId);

}
