package org.example.backendweride.platform.booking.domain.model.aggregates;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.Objects;

import org.example.backendweride.platform.booking.domain.model.commands.CreateBookingCommand;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;
import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingEntity;

/**
 * Add booking aggregate root.
 *
 */
public class Booking {

    private final String id;
    private final String customerId;
    private final String vehicleId;
    private final LocalDate date;
    private final LocalTime unlockTime;
    private final int durationMinutes;
    private final BigDecimal pricePerMinute;
    private final BigDecimal totalPrice;
    private String status; // draft, confirmed, cancelled

    private Booking(String id,
                    String customerId,
                    String vehicleId,
                    LocalDate date,
                    LocalTime unlockTime,
                    int durationMinutes,
                    BigDecimal pricePerMinute,
                    BigDecimal totalPrice,
                    String status) {
        this.id = Objects.requireNonNull(id, "id");
        this.customerId = Objects.requireNonNull(customerId, "customerId");
        this.vehicleId = Objects.requireNonNull(vehicleId, "vehicleId");
        this.date = Objects.requireNonNull(date, "date");
        this.unlockTime = Objects.requireNonNull(unlockTime, "unlockTime");
        this.durationMinutes = durationMinutes;
        this.pricePerMinute = Objects.requireNonNull(pricePerMinute, "pricePerMinute");
        this.totalPrice = Objects.requireNonNull(totalPrice, "totalPrice");
        this.status = Objects.requireNonNull(status, "status");
    }

    /**
     * Create a booking draft from the command and price per minute.
     */
    public static Booking createDraftFrom(SaveBookingDraftCommand cmd, BigDecimal pricePerMinute) {
        Objects.requireNonNull(cmd, "command");
        Objects.requireNonNull(pricePerMinute, "pricePerMinute");
        String id = cmd.draftId() == null || cmd.draftId().isBlank() ? java.util.UUID.randomUUID().toString() : cmd.draftId();
        BigDecimal total = pricePerMinute.multiply(BigDecimal.valueOf(cmd.durationMinutes()));
        return new Booking(id, cmd.customerId(), cmd.vehicleId(), cmd.date(), cmd.unlockTime(), cmd.durationMinutes(), pricePerMinute, total, "draft");
    }

    /**
     * Create a confirmed booking from the command and price per minute.
     */
    public static Booking createConfirmedFrom(CreateBookingCommand cmd, BigDecimal pricePerMinute) {
        Objects.requireNonNull(cmd, "command");
        Objects.requireNonNull(pricePerMinute, "pricePerMinute");
        String id = cmd.bookingId() == null || cmd.bookingId().isBlank() ? java.util.UUID.randomUUID().toString() : cmd.bookingId();
        BigDecimal total = pricePerMinute.multiply(BigDecimal.valueOf(cmd.durationMinutes()));
        return new Booking(id, cmd.customerId(), cmd.vehicleId(), cmd.date(), cmd.unlockTime(), cmd.durationMinutes(), pricePerMinute, total, "confirmed");
    }

    /**
     * Convert to JPA entity.
     */
    public BookingEntity toEntity() {
        BookingEntity e = new BookingEntity();
        e.setId(this.id);
        e.setCustomerId(this.customerId);
        e.setVehicleId(this.vehicleId);
        e.setDate(this.date);
        e.setUnlockTime(this.unlockTime);
        e.setDurationMinutes(this.durationMinutes);
        e.setPricePerMinute(this.pricePerMinute);
        e.setTotalPrice(this.totalPrice);
        e.setStatus(this.status);
        return e;
    }

    /**
     * Build from JPA entity.
     */
    public static Booking fromEntity(BookingEntity e) {
        if (e == null) return null;
        return new Booking(e.getId(), e.getCustomerId(), e.getVehicleId(), e.getDate(), e.getUnlockTime(), e.getDurationMinutes(), e.getPricePerMinute(), e.getTotalPrice(), e.getStatus());
    }

    // getters
    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getVehicleId() { return vehicleId; }
    public LocalDate getDate() { return date; }
    public LocalTime getUnlockTime() { return unlockTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public BigDecimal getPricePerMinute() { return pricePerMinute; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }

    // comportamiento de dominio mínimo
    public void confirm() {
        if ("confirmed".equals(this.status)) return;
        this.status = "confirmed";
    }

    public void cancel() {
        this.status = "cancelled";
    }

}
