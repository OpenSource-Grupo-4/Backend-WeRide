package org.example.backendweride.platform.booking.application.commandservice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.backendweride.platform.booking.domain.services.BookingDraftService;
import org.example.backendweride.platform.booking.domain.services.BookingCommandService;
import org.example.backendweride.platform.booking.domain.services.VehicleCatalogService;
import org.example.backendweride.platform.booking.domain.services.BookingDraftValidationService;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.model.commands.DeleteBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;
import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingRepository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Implementation of BookingDraftService to handle booking draft operations.
 *
 * @summary This service processes commands for saving, updating, and deleting booking drafts.
 */
@Service
public class BookingDraftServiceImpl implements BookingDraftService {

    private final BookingRepository bookingRepository;
    private final VehicleCatalogService vehicleCatalogService;
    private final BookingDraftValidationService validationService;

    public BookingDraftServiceImpl(
        BookingRepository bookingRepository,
        VehicleCatalogService vehicleCatalogService,
        BookingDraftValidationService validationService
    ) {
        this.bookingRepository = bookingRepository;
        this.vehicleCatalogService = vehicleCatalogService;
        this.validationService = validationService;
    }

    @Override
    @Transactional
    public BookingCommandService.SaveDraftResult saveDraft(SaveBookingDraftCommand command) {
        Booking booking = Booking.createDraftFrom(command);
        Booking saved = bookingRepository.save(booking);

        return new BookingCommandService.SaveDraftResult(saved.getBookingId(), true, "Draft saved successfully");
    }

    @Override
    @Transactional
    public BookingCommandService.SaveDraftResult updateDraft(SaveBookingDraftCommand command) {
        if (command.userId() == null) {
            return new BookingCommandService.SaveDraftResult(null, false, "User ID is required for update");
        }

        // Find draft by user and draft status
        var drafts = bookingRepository.findByUserIdAndStatus(command.userId(), "draft",
            org.springframework.data.domain.PageRequest.of(0, 1));

        if (drafts.isEmpty()) {
            return new BookingCommandService.SaveDraftResult(null, false, "Draft not found");
        }

        Booking existing = drafts.getContent().get(0);

        // Update all booking fields
        existing.setUserId(command.userId());
        existing.setVehicleId(command.vehicleId());
        existing.setStartLocationId(command.startLocationId());
        existing.setEndLocationId(command.endLocationId());
        existing.setReservedAt(command.reservedAt());
        existing.setStartDate(command.startDate());
        existing.setEndDate(command.endDate());
        existing.setActualStartDate(command.actualStartDate());
        existing.setActualEndDate(command.actualEndDate());
        existing.setStatus(command.status());
        existing.setTotalCost(command.totalCost());
        existing.setDiscount(command.discount());
        existing.setFinalCost(command.finalCost());
        existing.setPaymentMethod(command.paymentMethod());
        existing.setPaymentStatus(command.paymentStatus());
        existing.setDistance(command.distance());
        existing.setDuration(command.duration());
        existing.setAverageSpeed(command.averageSpeed());

        if (command.ratingScore() != null && command.ratingComment() != null) {
            existing.setRating(command.ratingScore(), command.ratingComment());
        }

        Booking saved = bookingRepository.save(existing);

        return new BookingCommandService.SaveDraftResult(saved.getBookingId(), true, "Draft updated successfully");
    }

    @Override
    @Transactional
    public BookingCommandService.SaveDraftResult deleteDraft(DeleteBookingDraftCommand command) {
        if (command.draftId() == null) {
            return new BookingCommandService.SaveDraftResult(null, false, "Draft ID is required");
        }

        var existingOpt = bookingRepository.findByBookingId(command.draftId());
        if (existingOpt.isEmpty()) {
            return new BookingCommandService.SaveDraftResult(null, false, "Draft not found");
        }

        Booking existing = existingOpt.get();

        // Verify the draft belongs to the user
        if (!existing.getUserId().equals(command.userId())) {
            return new BookingCommandService.SaveDraftResult(null, false, "Draft does not belong to this user");
        }

        if (!"draft".equals(existing.getStatus())) {
            return new BookingCommandService.SaveDraftResult(null, false, "Cannot delete a non-draft booking");
        }

        bookingRepository.delete(existing);

        return new BookingCommandService.SaveDraftResult(command.draftId(), true, "Draft deleted successfully");
    }

    @Override
    public Optional<SaveBookingDraftCommand> getDraftById(Long draftId) {
        if (draftId == null) {
            return Optional.empty();
        }

        var opt = bookingRepository.findByBookingId(draftId);
        if (opt.isEmpty() || !"draft".equals(opt.get().getStatus())) {
            return Optional.empty();
        }

        Booking booking = opt.get();
        return Optional.of(new SaveBookingDraftCommand(
            booking.getUserId(),
            booking.getVehicleId(),
            booking.getStartLocationId(),
            booking.getEndLocationId(),
            booking.getReservedAt(),
            booking.getStartDate(),
            booking.getEndDate(),
            booking.getActualStartDate(),
            booking.getActualEndDate(),
            booking.getStatus(),
            booking.getTotalCost(),
            booking.getDiscount(),
            booking.getFinalCost(),
            booking.getPaymentMethod(),
            booking.getPaymentStatus(),
            booking.getDistance(),
            booking.getDuration(),
            booking.getAverageSpeed(),
            booking.getRating() != null ? booking.getRating().getScore() : null,
            booking.getRating() != null ? booking.getRating().getComment() : null
        ));
    }
}

