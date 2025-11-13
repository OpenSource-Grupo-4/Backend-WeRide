package org.example.backendweride.platform.booking.interfaces;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.example.backendweride.platform.booking.interfaces.resources.CreateBookingResource;
import org.example.backendweride.platform.booking.interfaces.resources.BookingResource;
import org.example.backendweride.platform.booking.interfaces.transform.CreateBookingCommandFromResourceAssembler;
import org.example.backendweride.platform.booking.domain.services.BookingCommandService;
import org.example.backendweride.platform.booking.application.queryservice.BookingQueryServiceImpl;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.model.commands.CreateBookingCommand;
import org.example.backendweride.platform.booking.domain.model.queries.GetBookingByIdQuery;
import org.example.backendweride.platform.booking.domain.model.queries.SearchBookingsQuery;
import org.example.backendweride.platform.booking.domain.model.queries.GetBookingDraftsByCustomerQuery;

import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * BookingController handles HTTP requests related to bookings.
 *
 * @summary This controller provides endpoints for creating bookings, saving drafts,
 *          retrieving bookings by ID, searching bookings, and getting drafts by customer.
 */
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingCommandService commandService;
    private final BookingQueryServiceImpl bookingQueryService;

    public BookingController(BookingCommandService commandService, BookingQueryServiceImpl bookingQueryService) {
        this.commandService = commandService;
        this.bookingQueryService = bookingQueryService;
    }

    @PostMapping("/draft")
    public ResponseEntity<BookingResource> saveDraft(@RequestBody CreateBookingResource resource) {
        // PD:Map resource to SaveBookingDraftCommand (user sends vehicleType which is the vehicle id)
        SaveBookingDraftCommand cmd = new SaveBookingDraftCommand(
            null,
            resource.customerId(),
            resource.vehicleType(),
            resource.date(),
            resource.unlockTime(),
            resource.durationMinutes()
        );

        var result = commandService.saveDraft(cmd);
        if (!result.success()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<BookingResource> resp = bookingQueryService.getBookingById(new GetBookingByIdQuery(result.draftId()));
        return resp.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BookingResource> createBooking(@RequestBody CreateBookingResource resource) {
        CreateBookingCommand cmd = CreateBookingCommandFromResourceAssembler.toCommand(resource);
        var result = commandService.createBooking(cmd);
        if (!result.success()) return ResponseEntity.badRequest().build();

        Optional<BookingResource> resp = bookingQueryService.getBookingById(new GetBookingByIdQuery(result.bookingId()));
        return resp.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResource> getBookingById(@PathVariable("id") String id) {
        var opt = bookingQueryService.getBookingById(new GetBookingByIdQuery(id));
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<BookingResource>> searchBookings(
        @RequestParam(required = false) String customerId,
        @RequestParam(required = false) String vehicleId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String startAtFrom,
        @RequestParam(required = false) String startAtTo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        LocalDate from = null;
        LocalDate to = null;
        try {
            if (startAtFrom != null) from = LocalDate.parse(startAtFrom);
            if (startAtTo != null) to = LocalDate.parse(startAtTo);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().build();
        }

        SearchBookingsQuery q = new SearchBookingsQuery(customerId, vehicleId, status, from, to, page, size);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        var results = bookingQueryService.searchBookings(q, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/drafts")
    public ResponseEntity<Page<BookingResource>> getDraftsByCustomer(
        @RequestParam String customerId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        GetBookingDraftsByCustomerQuery q = new GetBookingDraftsByCustomerQuery(customerId, page, size);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        var results = bookingQueryService.getBookingDraftsByCustomer(q, pageable);
        return ResponseEntity.ok(results);
    }

}
