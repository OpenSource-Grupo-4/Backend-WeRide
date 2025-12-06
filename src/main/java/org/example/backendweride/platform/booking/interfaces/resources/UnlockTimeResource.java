package org.example.backendweride.platform.booking.interfaces.resources;

/**
 * UnlockTime value object for booking resources.
 *
 * @summary This record encapsulates the time details for unlocking a vehicle.
 */
public record UnlockTimeResource(
    int hour,
    int minute,
    int second,
    int nano
) {
    /**
     * Convert to LocalTime.
     */
    public java.time.LocalTime toLocalTime() {
        return java.time.LocalTime.of(hour, minute, second, nano);
    }
}

