package org.example.backendweride.platform.booking.application.queryservice;

import org.example.backendweride.platform.booking.domain.model.queries.SearchBookingsQuery;
import org.example.backendweride.platform.booking.domain.model.queries.GetPendingBookingsByUserQuery;
import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;
import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingQueryServiceImplTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final BookingQueryServiceImpl service = new BookingQueryServiceImpl(bookingRepository);
    private final Pageable pageable = PageRequest.of(0, 20);
    private final Page<org.example.backendweride.platform.booking.domain.model.aggregates.Booking> emptyPage = new PageImpl<>(List.of());

    @Test
    void searchBookings_withoutCustomerId_returnsEmptyPage() {
        var result = service.searchBookings(new SearchBookingsQuery(null, 5L, "confirmed", null, null, 0, 20), pageable);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchBookings_withCustomerIdAndStatus_filtersByBoth() {
        when(bookingRepository.findByUserIdAndStatus(eq(42L), eq("confirmed"), any())).thenReturn(emptyPage);

        service.searchBookings(new SearchBookingsQuery(42L, 5L, "confirmed", null, null, 0, 20), pageable);

        verify(bookingRepository).findByUserIdAndStatus(42L, "confirmed", pageable);
    }

    @Test
    void searchBookings_withCustomerIdOnly_filtersByUser() {
        when(bookingRepository.findByUserId(eq(42L), any())).thenReturn(emptyPage);

        service.searchBookings(new SearchBookingsQuery(42L, null, null, null, null, 0, 20), pageable);

        verify(bookingRepository).findByUserId(42L, pageable);
    }

    @Test
    void searchBookings_withCustomerIdAndVehicleId_filtersByBoth() {
        when(bookingRepository.findByUserIdAndVehicleId(eq(42L), eq(5L), any())).thenReturn(emptyPage);

        service.searchBookings(new SearchBookingsQuery(42L, 5L, null, null, null, 0, 20), pageable);

        verify(bookingRepository).findByUserIdAndVehicleId(42L, 5L, pageable);
    }

    @Test
    void searchBookings_withCustomerIdAndDateRange_filtersByBoth() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(23, 59, 59);
        when(bookingRepository.findByUserIdAndStartDateBetween(eq(42L), eq(fromDateTime), eq(toDateTime), any())).thenReturn(emptyPage);

        service.searchBookings(new SearchBookingsQuery(42L, null, null, from, to, 0, 20), pageable);

        verify(bookingRepository).findByUserIdAndStartDateBetween(42L, fromDateTime, toDateTime, pageable);
    }

    @Test
    void getPendingBookingsByUser_doesNotDoubleCountAcrossStatuses() {
        var pageable = PageRequest.of(0, 10);
        var userId = 7L;
        var pendingBooking = bookingWithStatus(1L, userId, "pending");
        var confirmedBooking = bookingWithStatus(2L, userId, "confirmed");

        when(bookingRepository.findByUserIdAndStatus(eq(userId), eq("pending"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(pendingBooking), pageable, 1));
        when(bookingRepository.findByUserIdAndStatus(eq(userId), eq("confirmed"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(confirmedBooking), pageable, 1));

        var result = service.getPendingBookingsByUser(new GetPendingBookingsByUserQuery(userId), pageable);

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().size() <= pageable.getPageSize());
    }

    @Test
    void getPendingBookingsByUser_splitsPageSizeBetweenTheTwoStatuses() {
        var pageable = PageRequest.of(0, 10);
        var userId = 7L;

        when(bookingRepository.findByUserIdAndStatus(eq(userId), eq("pending"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(bookingRepository.findByUserIdAndStatus(eq(userId), eq("confirmed"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        service.getPendingBookingsByUser(new GetPendingBookingsByUserQuery(userId), pageable);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookingRepository, times(2)).findByUserIdAndStatus(eq(userId), anyString(), pageableCaptor.capture());
        for (Pageable used : pageableCaptor.getAllValues()) {
            assertEquals(5, used.getPageSize());
        }
    }

    private Booking bookingWithStatus(Long bookingId, Long userId, String status) {
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setStatus(status);
        booking.setBookingId(bookingId);
        return booking;
    }
}
