package org.example.backendweride.platform.booking.interfaces;

import org.example.backendweride.platform.booking.domain.model.commands.DeleteBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.model.queries.GetBookingDraftsByCustomerQuery;
import org.example.backendweride.platform.booking.domain.model.queries.GetBookingsByUserIdQuery;
import org.example.backendweride.platform.booking.domain.model.queries.GetCompletedBookingsByUserQuery;
import org.example.backendweride.platform.booking.domain.model.queries.GetPendingBookingsByUserQuery;
import org.example.backendweride.platform.booking.domain.services.BookingCommandService;
import org.example.backendweride.platform.booking.domain.services.BookingDraftService;
import org.example.backendweride.platform.booking.domain.services.BookingQueryService;
import org.example.backendweride.platform.booking.interfaces.resources.BookingResource;
import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingControllerTest {

    private final BookingCommandService commandService = mock(BookingCommandService.class);
    private final BookingQueryService bookingQueryService = mock(BookingQueryService.class);
    private final BookingDraftService draftService = mock(BookingDraftService.class);
    private final AuthenticatedAccountProvider authenticatedAccountProvider = mock(AuthenticatedAccountProvider.class);

    private final BookingController controller = new BookingController(
            commandService, bookingQueryService, draftService, authenticatedAccountProvider);

    private static final Page<BookingResource> EMPTY_PAGE = new PageImpl<>(List.of());

    @Test
    void getBookingsByUserId_usesAuthenticatedUserId_notPathVariable() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(bookingQueryService.getBookingsByUserId(any(), any())).thenReturn(EMPTY_PAGE);

        controller.getBookingsByUserId(999L, 0, 20);

        ArgumentCaptor<GetBookingsByUserIdQuery> captor = ArgumentCaptor.forClass(GetBookingsByUserIdQuery.class);
        verify(bookingQueryService).getBookingsByUserId(captor.capture(), any(Pageable.class));
        assertEquals(42L, captor.getValue().userId());
    }

    @Test
    void getPendingBookingsByUser_usesAuthenticatedUserId_notPathVariable() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(bookingQueryService.getPendingBookingsByUser(any(), any())).thenReturn(EMPTY_PAGE);

        controller.getPendingBookingsByUser(999L, 0, 20);

        ArgumentCaptor<GetPendingBookingsByUserQuery> captor = ArgumentCaptor.forClass(GetPendingBookingsByUserQuery.class);
        verify(bookingQueryService).getPendingBookingsByUser(captor.capture(), any(Pageable.class));
        assertEquals(42L, captor.getValue().userId());
    }

    @Test
    void getCompletedBookingsByUser_usesAuthenticatedUserId_notPathVariable() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(bookingQueryService.getCompletedBookingsByUser(any(), any())).thenReturn(EMPTY_PAGE);

        controller.getCompletedBookingsByUser(999L, 0, 20);

        ArgumentCaptor<GetCompletedBookingsByUserQuery> captor = ArgumentCaptor.forClass(GetCompletedBookingsByUserQuery.class);
        verify(bookingQueryService).getCompletedBookingsByUser(captor.capture(), any(Pageable.class));
        assertEquals(42L, captor.getValue().userId());
    }

    @Test
    void getDraftsByCustomer_usesAuthenticatedUserId_notQueryParam() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(bookingQueryService.getBookingDraftsByCustomer(any(), any())).thenReturn(EMPTY_PAGE);

        controller.getDraftsByCustomer(999L, 0, 20);

        ArgumentCaptor<GetBookingDraftsByCustomerQuery> captor = ArgumentCaptor.forClass(GetBookingDraftsByCustomerQuery.class);
        verify(bookingQueryService).getBookingDraftsByCustomer(captor.capture(), any(Pageable.class));
        assertEquals(42L, captor.getValue().customerId());
    }

    @Test
    void deleteDraft_usesAuthenticatedUserId_notParsedFromUsername() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(draftService.deleteDraft(any())).thenReturn(new BookingCommandService.SaveDraftResult(555L, true, "Draft deleted successfully"));

        controller.deleteDraft(555L);

        ArgumentCaptor<DeleteBookingDraftCommand> captor = ArgumentCaptor.forClass(DeleteBookingDraftCommand.class);
        verify(draftService).deleteDraft(captor.capture());
        assertEquals(42L, captor.getValue().userId());
        assertEquals(555L, captor.getValue().draftId());
    }
}
