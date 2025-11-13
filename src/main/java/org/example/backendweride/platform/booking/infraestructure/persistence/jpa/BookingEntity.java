package org.example.backendweride.platform.booking.infraestructure.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

/**
 * Booking JPA entity.
 *
 * @summary This entity represents a booking record in the database.
 */
@Entity
@Table(name = "bookings")
public class BookingEntity {

    @Id
    private String id;

    private String customerId;
    private String vehicleId;
    private LocalDate date;
    private LocalTime unlockTime;
    private int durationMinutes;
    private BigDecimal pricePerMinute;
    private BigDecimal totalPrice;
    private String status; // draft, confirmed, cancelled

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getUnlockTime() { return unlockTime; }
    public void setUnlockTime(LocalTime unlockTime) { this.unlockTime = unlockTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public BigDecimal getPricePerMinute() { return pricePerMinute; }
    public void setPricePerMinute(BigDecimal pricePerMinute) { this.pricePerMinute = pricePerMinute; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

