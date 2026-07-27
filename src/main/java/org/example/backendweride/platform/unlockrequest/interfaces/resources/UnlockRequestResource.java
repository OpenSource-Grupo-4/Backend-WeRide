package org.example.backendweride.platform.unlockrequest.interfaces.resources;

import org.example.backendweride.platform.location.domain.valueobjects.Coordinates;

import java.util.Date;

public record UnlockRequestResource(
        String id,
        String userId,
        Long vehicleId,
        Long bookingId,
        Date requestedAt,
        Date scheduledUnlockTime,
        Date actualUnlockTime,
        String status,
        String method,
        Coordinates location,
        String unlockCode,
        int attempts,
        String errorMessage
) {}
