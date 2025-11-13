package org.example.backendweride.platform.garage.interfaces.rest.resources;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResource {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long userId;
    private Long vehicleId;
}
