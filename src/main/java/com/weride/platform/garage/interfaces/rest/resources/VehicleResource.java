package com.weride.platform.garage.interfaces.rest.resources;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResource {
    private Long id;
    private String name;
    private String brand;
    private Double price;
    private Double rating;
    private Boolean available;
    private Boolean favorite;
    private String imageUrl;
}
