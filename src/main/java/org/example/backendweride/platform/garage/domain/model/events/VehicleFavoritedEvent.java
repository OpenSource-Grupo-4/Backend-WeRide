package org.example.backendweride.platform.garage.domain.model.events;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleFavoritedEvent {
    private Long vehicleId;
    private Long userId;
}
