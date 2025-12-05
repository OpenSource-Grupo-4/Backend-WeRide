package org.example.backendweride.platform.plans.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Plans entity representing subscription plans.
 * @summary This entity defines the structure and attributes of subscription plans available in the system.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plans {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String planId;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private BigDecimal pricePerMinute;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private Integer durationDays;

    @Column(nullable = false)
    private Integer maxTripsPerDay;

    @Column(nullable = false)
    private Integer maxMinutesPerTrip;

    @Column(nullable = false)
    private Integer freeMinutesPerMonth;

    @Column(nullable = false)
    private BigDecimal discountPercentage;

    @ElementCollection
    private List<String> benefits;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Boolean isPopular;

    @Column(nullable = false)
    private Boolean isActive;
}
