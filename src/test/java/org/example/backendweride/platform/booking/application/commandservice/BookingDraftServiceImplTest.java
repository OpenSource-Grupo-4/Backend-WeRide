package org.example.backendweride.platform.booking.application.commandservice;

import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.services.BookingDraftValidationService;
import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingDraftServiceImplTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final BookingDraftValidationService validationService = mock(BookingDraftValidationService.class);
    private final BookingDraftServiceImpl service =
            new BookingDraftServiceImpl(bookingRepository, validationService);

    private SaveBookingDraftCommand draftCommand(LocalDateTime startDate) {
        return new SaveBookingDraftCommand(
                1L, 2L, 3L, 4L,
                LocalDateTime.now(),
                startDate,
                LocalDateTime.now(),
                null, null,
                "draft", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "card", "pending",
                0.0, 30, 0.0,
                null, null
        );
    }

    @Test
    void saveDraft_rejectsPastStartDate() {
        var pastCommand = draftCommand(LocalDateTime.now().minusDays(1));
        when(validationService.validateForSave(pastCommand))
                .thenReturn(new BookingDraftValidationService.ValidationResult(false, "Start date cannot be in the past"));

        var result = service.saveDraft(pastCommand);

        assertFalse(result.success());
        assertEquals("Start date cannot be in the past", result.message());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void saveDraft_savesDraft_whenValidationPasses() {
        var command = draftCommand(LocalDateTime.now().plusDays(1));
        when(validationService.validateForSave(command))
                .thenReturn(new BookingDraftValidationService.ValidationResult(true, "Validation successful"));

        Booking booking = Booking.createDraftFrom(command);
        when(bookingRepository.save(any())).thenReturn(booking);

        var result = service.saveDraft(command);

        assertTrue(result.success());
        assertEquals("Draft saved successfully", result.message());
    }
}
