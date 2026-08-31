package org.example.backendweride.platform.booking.domain.model.aggregates;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.backendweride.platform.booking.domain.model.commands.CreateBookingCommand;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.model.commands.UpdateBookingCommand;
import org.example.backendweride.platform.booking.domain.model.entities.BookingIssue;
import org.example.backendweride.platform.booking.domain.model.valueobjects.Rating;
import org.example.backendweride.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.util.stream.Collectors;

/**
 * Booking aggregate root
 * This class represents the aggregate root for the Booking entity.
 *
 * @see AuditableAbstractAggregateRoot
 */
@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking extends AuditableAbstractAggregateRoot<Booking> {

    @NotNull
    @Column(unique = true, nullable = false)
    private Long bookingId;

    @NotNull
    private Long userId;

    @NotNull
    private Long vehicleId;

    private Long startLocationId;
    private Long endLocationId;

    private LocalDateTime reservedAt;

    @NotNull
    private LocalDateTime startDate;

    private LocalDateTime endDate;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualEndDate;

    @NotBlank
    private String status; // pending, confirmed, in_progress, completed, cancelled

    private BigDecimal totalCost;
    private BigDecimal discount;
    private BigDecimal finalCost;

    @NotBlank
    private String paymentMethod; // credit_card, debit_card, paypal, etc.

    @NotBlank
    private String paymentStatus; // pending, paid, failed, refunded

    private Double distance;
    private Integer duration; // in minutes
    private Double averageSpeed;

    @Embedded
    private Rating rating;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingIssue> issueEntities = new ArrayList<>();

    public List<String> getIssues() {
        return issueEntities.stream().map(BookingIssue::getIssue).collect(Collectors.toList());
    }

    public Booking() {
        this.issueEntities = new ArrayList<>();
    }

    public Booking(Long userId, Long vehicleId, Long startLocationId, Long endLocationId,
                   LocalDateTime startDate, LocalDateTime endDate, String paymentMethod, String paymentStatus) {
        this.bookingId = generateBookingId();
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.startLocationId = startLocationId;
        this.endLocationId = endLocationId;
        this.reservedAt = LocalDateTime.now();
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "pending";
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.issueEntities = new ArrayList<>();
        this.discount = BigDecimal.ZERO;
    }

    /**
     * Create a booking draft from the command.
     *
     * @param cmd the SaveBookingDraftCommand
     * @return the Booking instance in draft status
     */
    public static Booking createDraftFrom(SaveBookingDraftCommand cmd) {
        Booking booking = new Booking();
        booking.bookingId = generateBookingId();
        booking.userId = cmd.userId();
        booking.vehicleId = cmd.vehicleId();
        booking.startLocationId = cmd.startLocationId();
        booking.endLocationId = cmd.endLocationId();
        booking.reservedAt = cmd.reservedAt();
        booking.startDate = cmd.startDate();
        booking.endDate = cmd.endDate();
        booking.actualStartDate = cmd.actualStartDate();
        booking.actualEndDate = cmd.actualEndDate();
        booking.status = cmd.status();
        booking.totalCost = cmd.totalCost();
        booking.discount = cmd.discount();
        booking.finalCost = cmd.finalCost();
        booking.paymentMethod = cmd.paymentMethod();
        booking.paymentStatus = cmd.paymentStatus();
        booking.distance = cmd.distance();
        booking.duration = cmd.duration();
        booking.averageSpeed = cmd.averageSpeed();

        // Set rating if provided
        if (cmd.ratingScore() != null && cmd.ratingComment() != null) {
            booking.rating = new Rating();
            booking.rating.setScore(cmd.ratingScore());
            booking.rating.setComment(cmd.ratingComment());
        }

        booking.issueEntities = new ArrayList<>();
        return booking;
    }

    /**
     * Create a confirmed booking from the command.
     *
     * @param cmd the CreateBookingCommand
     * @return the Booking instance with all provided data
     */
    public static Booking createConfirmedFrom(CreateBookingCommand cmd) {
        Booking booking = new Booking();
        booking.bookingId = generateBookingId();  // Auto-generated
        booking.userId = cmd.userId();
        booking.vehicleId = cmd.vehicleId();
        booking.startLocationId = cmd.startLocationId();
        booking.endLocationId = cmd.endLocationId();
        booking.reservedAt = cmd.reservedAt();
        booking.startDate = cmd.startDate();
        booking.endDate = cmd.endDate();
        booking.actualStartDate = cmd.actualStartDate();
        booking.actualEndDate = cmd.actualEndDate();
        booking.status = cmd.status();
        booking.totalCost = cmd.totalCost();
        booking.discount = cmd.discount();
        booking.finalCost = cmd.finalCost();
        booking.paymentMethod = cmd.paymentMethod();
        booking.paymentStatus = cmd.paymentStatus();
        booking.distance = cmd.distance();
        booking.duration = cmd.duration();
        booking.averageSpeed = cmd.averageSpeed();

        // Set rating if provided
        if (cmd.ratingScore() != null && cmd.ratingComment() != null) {
            booking.rating = new Rating();
            booking.rating.setScore(cmd.ratingScore());
            booking.rating.setComment(cmd.ratingComment());
        }

        booking.issueEntities = new ArrayList<>();
        return booking;
    }

    public void updateFrom(UpdateBookingCommand command) {
        if (command.vehicleId() != null) vehicleId = command.vehicleId();
        if (command.startLocationId() != null) startLocationId = command.startLocationId();
        if (command.endLocationId() != null) endLocationId = command.endLocationId();
        if (command.reservedAt() != null) reservedAt = command.reservedAt();
        if (command.startDate() != null) startDate = command.startDate();
        if (command.endDate() != null) endDate = command.endDate();
        if (command.actualStartDate() != null) actualStartDate = command.actualStartDate();
        if (command.actualEndDate() != null) actualEndDate = command.actualEndDate();
        if (command.status() != null) status = command.status();
        if (command.totalCost() != null) totalCost = command.totalCost();
        if (command.discount() != null) discount = command.discount();
        if (command.finalCost() != null) finalCost = command.finalCost();
        if (command.paymentMethod() != null) paymentMethod = command.paymentMethod();
        if (command.paymentStatus() != null) paymentStatus = command.paymentStatus();
        if (command.distance() != null) distance = command.distance();
        if (command.duration() != null) duration = command.duration();
        if (command.averageSpeed() != null) averageSpeed = command.averageSpeed();
        if (command.ratingScore() != null || command.ratingComment() != null) {
            setRating(command.ratingScore(), command.ratingComment());
        }
    }

    /**
     * Generate a booking ID.
     *
     * @return the generated booking ID
     */
    private static Long generateBookingId() {
        return java.util.UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }

    /**
     * Confirm the booking.
     */
    public void confirm() {
        if ("confirmed".equals(this.status)) return;
        this.status = "confirmed";
    }

    /**
     * Cancel the booking.
     */
    public void cancel() {
        this.status = "cancelled";
    }

    /**
     * Start the booking.
     */
    public void start() {
        this.status = "in_progress";
        this.actualStartDate = LocalDateTime.now();
    }

    /**
     * Complete the booking.
     */
    public void complete() {
        this.status = "completed";
        this.actualEndDate = LocalDateTime.now();
    }

    /**
     * Calculate total cost based on price per minute.
     *
     * @param pricePerMinute the price per minute
     */
    public void calculateCost(BigDecimal pricePerMinute) {
        if (this.duration != null && pricePerMinute != null) {
            this.totalCost = pricePerMinute.multiply(BigDecimal.valueOf(this.duration));
            this.finalCost = this.totalCost.subtract(this.discount != null ? this.discount : BigDecimal.ZERO);
        }
    }

    /**
     * Add an issue to the booking.
     *
     * @param issue the issue description
     */
    public void addIssue(String issue) {
        this.issueEntities.add(new BookingIssue(this, issue));
    }
    /**
     * Set rating for the booking.
     *
     * @param score the rating score
     * @param comment the rating comment
     */
    public void setRating(Integer score, String comment) {
        this.rating = new Rating(score, comment);
    }
}
