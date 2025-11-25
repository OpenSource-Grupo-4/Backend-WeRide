package org.example.backendweride.platform.garage.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String brand;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Double price;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean favorite = false;

    @Column(length = 255)
    private String imageUrl;

    @Column(length = 20)
    private String licensePlate;

    @Column(length = 50)
    private String color;

    @Column(length = 20)
    private String fuelType;

    @Column
    private Integer mileage;
}
