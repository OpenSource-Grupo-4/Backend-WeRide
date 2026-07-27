package org.example.backendweride.platform.unlockrequest.domain.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.example.backendweride.platform.location.domain.valueobjects.Coordinates;

import java.util.Date;
import java.util.UUID;

@Getter
@Entity
@Table(name = "unlock_requests")
public class UnlockRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String publicId = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String userId;
    private Long vehicleId;
    private Long bookingId;
    private Date requestedAt;
    private Date scheduledUnlockTime;
    private Date actualUnlockTime;
    private String status;
    private String method;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "latitude")),
            @AttributeOverride(name = "lng", column = @Column(name = "longitude"))
    })
    private Coordinates location;

    private String unlockCode;
    private int attempts;
    private String errorMessage;

    protected UnlockRequest() {}

    public UnlockRequest(String userId, Long vehicleId, Long bookingId, Date requestedAt,
                         Date scheduledUnlockTime, Date actualUnlockTime, String status,
                         String method, Coordinates location, String unlockCode,
                         int attempts, String errorMessage) {
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.bookingId = bookingId;
        this.requestedAt = requestedAt;
        this.scheduledUnlockTime = scheduledUnlockTime;
        this.actualUnlockTime = actualUnlockTime;
        this.status = status;
        this.method = method;
        this.location = location;
        this.unlockCode = unlockCode;
        this.attempts = attempts;
        this.errorMessage = errorMessage;
    }

    public void update(Long vehicleId, Long bookingId, Date scheduledUnlockTime, Date actualUnlockTime,
                       String status, String method, Coordinates location, String unlockCode,
                       Integer attempts, String errorMessage) {
        if (vehicleId != null) this.vehicleId = vehicleId;
        if (bookingId != null) this.bookingId = bookingId;
        if (scheduledUnlockTime != null) this.scheduledUnlockTime = scheduledUnlockTime;
        if (actualUnlockTime != null) this.actualUnlockTime = actualUnlockTime;
        if (status != null) this.status = status;
        if (method != null) this.method = method;
        if (location != null) this.location = location;
        if (unlockCode != null) this.unlockCode = unlockCode;
        if (attempts != null) this.attempts = attempts;
        if (errorMessage != null) this.errorMessage = errorMessage;
    }
}
