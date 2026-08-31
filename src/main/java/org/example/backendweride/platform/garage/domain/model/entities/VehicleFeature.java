package org.example.backendweride.platform.garage.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backendweride.platform.garage.domain.model.aggregates.Vehicle;

@Entity
@Table(name = "vehicle_features")
@Getter
@NoArgsConstructor
public class VehicleFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "feature", nullable = false, length = 255)
    private String feature;

    public VehicleFeature(Vehicle vehicle, String feature) {
        this.vehicle = vehicle;
        this.feature = feature;
    }
}
