package com.weride.platform.garage.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String brand;
    private Double price;
    private Double rating;
    private Boolean available;
    private Boolean favorite;
    private String imageUrl;
}
