package org.example.backendweride.platform.booking.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * BookingController handles HTTP requests related to bookings.
 *
 * @summary This controller provides endpoints for creating bookings, saving drafts,
 *          retrieving bookings by ID, searching bookings, and getting drafts by customer.
 */
@RestController
@RequestMapping(value = "/api/v1/bookings", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Booking", description = "Bookings Manager")
public class BookingController {

    private final BookingCommandService commandService;
    private final BookingQueryServiceImpl bookingQueryService;

    public BookingController(BookingCommandService commandService, BookingQueryServiceImpl bookingQueryService) {
        this.commandService = commandService;
        this.bookingQueryService = bookingQueryService;
    }

    /**
     * Save a booking draft.
     * @param resource The booking draft details.
     * @return ResponseEntity containing the saved booking draft or an error status.
     */
    @PostMapping("/draft")
    @Operation(summary = "Create a new booking", description = "Create a new booking with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "404", description = "Booking could not be found")
    })
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

    /**
     * Create a new booking.
     * @param resource The booking details.
     * @return ResponseEntity containing the created booking or an error status.
     */
    @PostMapping
    @Operation(summary = "Create a new booking", description = "Create a new booking with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "404", description = "Booking could not be found")
    })
    public ResponseEntity<BookingResource> createBooking(@RequestBody CreateBookingResource resource) {
        CreateBookingCommand cmd = CreateBookingCommandFromResourceAssembler.toCommand(resource);
        var result = commandService.createBooking(cmd);
        if (!result.success()) return ResponseEntity.badRequest().build();

        Optional<BookingResource> resp = bookingQueryService.getBookingById(new GetBookingByIdQuery(result.bookingId()));
        return resp.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get a booking by its ID.
     * @param id The unique identifier of the booking.
     * @return ResponseEntity containing the booking or an error status.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Retrieve a booking by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingResource> getBookingById(@PathVariable("id") String id) {
        var opt = bookingQueryService.getBookingById(new GetBookingByIdQuery(id));
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Search for bookings based on various criteria.
     * @param customerId Filter by customer ID (optional).
     * @param vehicleId Filter by vehicle ID (optional).
     * @param status Filter by booking status (optional).
     * @param startAtFrom Filter bookings starting from this date (optional).
     * @param startAtTo Filter bookings up to this date (optional).
     * @param page Page number for pagination (default is 0).
     * @param size Page size for pagination (default is 20).
     * @return ResponseEntity containing a page of bookings matching the criteria.
     */
    @GetMapping
    @Operation(summary = "Search bookings", description = "Search for bookings based on various criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
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

    /**
     * Get booking drafts for a specific customer.
     * @param customerId The unique identifier of the customer.
     * @param page Page number for pagination (default is 0).
     * @param size Page size for pagination (default is 20).
     * @return ResponseEntity containing a page of booking drafts for the customer.
     */
    @GetMapping("/drafts")
    @Operation(summary = "Get booking drafts by customer", description = "Retrieve booking drafts for a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking drafts retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid customer ID")
    })
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
