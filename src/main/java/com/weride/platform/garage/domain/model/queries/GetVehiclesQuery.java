package com.weride.platform.garage.domain.model.queries;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetVehiclesQuery {
    private Boolean onlyFavorites;
}
