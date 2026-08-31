package org.example.backendweride.platform.garage.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backendweride.platform.garage.domain.model.commands.CreateVehicleCommand;
import org.example.backendweride.platform.garage.domain.model.commands.UpdateVehicleCommand;
import org.example.backendweride.platform.garage.domain.model.entities.VehicleFeature;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    private String brand;
    @Getter
    private String model;
    @Getter
    private Integer year;
    @Getter
    private Integer battery;
    @Getter
    private Integer maxSpeed;
    @Getter
    @Column(name = "vehicle_range")
    private Integer range;
    @Getter
    private double weight;
    @Getter
    private String color;
    @Getter
    private String licensePlate;
    @Getter
    private String location;
    @Getter
    private String status;
    @Getter
    private String type;
    @Getter
    private String companyId;
    @Getter
    private double pricePerMinute;

    @Column(length = 500)
    @Getter
    private String image;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehicleFeature> featureEntities = new ArrayList<>();

    public List<String> getFeatures() {
        return featureEntities.stream().map(VehicleFeature::getFeature).collect(Collectors.toList());
    }

    @Getter
    private String maintenanceStatus;
    @Getter
    private Date lastMaintenance;
    @Getter
    private Date nextMaintenance;
    @Getter
    private double totalKilometers;
    @Getter
    private double rating;

    protected Vehicle() {}

    public Vehicle(CreateVehicleCommand command) {
        this.brand = command.brand();
        this.model = command.model();
        this.year = command.year();
        this.battery = command.battery();
        this.maxSpeed = command.maxSpeed();
        this.range = command.range();
        this.weight = command.weight();
        this.color = command.color();
        this.licensePlate = command.licensePlate();
        this.location = command.location();
        this.status = command.status();
        this.type = command.type();
        this.companyId = command.companyId();
        this.pricePerMinute = command.pricePerMinute();
        this.image = command.image();
        this.featureEntities = toFeatureEntities(command.features());
        this.maintenanceStatus = command.maintenanceStatus();
        this.lastMaintenance = command.lastMaintenance();
        this.nextMaintenance = command.nextMaintenance();
        this.totalKilometers = command.totalKilometers();
        this.rating = command.rating();
    }

    public Vehicle updateInformation(UpdateVehicleCommand command) {
        this.brand = command.brand();
        this.model = command.model();
        this.year = command.year();
        this.battery = command.battery();
        this.maxSpeed = command.maxSpeed();
        this.range = command.range();
        this.weight = command.weight();
        this.color = command.color();
        this.licensePlate = command.licensePlate();
        this.location = command.location();
        this.status = command.status();
        this.type = command.type();
        this.companyId = command.companyId();
        this.pricePerMinute = command.pricePerMinute();
        this.image = command.image();
        this.featureEntities = toFeatureEntities(command.features());
        this.maintenanceStatus = command.maintenanceStatus();
        this.lastMaintenance = command.lastMaintenance();
        this.nextMaintenance = command.nextMaintenance();
        this.totalKilometers = command.totalKilometers();
        this.rating = command.rating();
        return this;
    }

    private List<VehicleFeature> toFeatureEntities(List<String> features) {
        if (features == null) return new ArrayList<>();
        return features.stream().map(feature -> new VehicleFeature(this, feature)).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle other = (Vehicle) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}