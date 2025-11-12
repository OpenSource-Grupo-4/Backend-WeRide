package com.weride.platform.garage.domain.model.commands;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddFavoriteCommand {
    private Long vehicleId;
}
