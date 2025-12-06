package org.example.backendweride.platform.garage.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backendweride.platform.garage.domain.model.commands.CreateVehicleCommand;
import org.example.backendweride.platform.garage.domain.model.commands.UpdateVehicleCommand;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;
    private Integer year;
    private Integer battery;
    private Integer maxSpeed;

    @Column(name = "vehicle_range")
    private Integer range;

    private Double weight;
    private String color;
    private String licensePlate;
    private String location;
    private String status;
    private String type;
    private String companyId;
    private Double pricePerMinute;

    @Column(length = 500)
    private String image;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vehicle_features", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "feature")
    private List<String> features;

    private String maintenanceStatus;
    private Date lastMaintenance;
    private Date nextMaintenance;
    private Double totalKilometers;
    private Double rating;

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
        this.features = command.features();
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
        this.features = command.features();
        this.maintenanceStatus = command.maintenanceStatus();
        this.lastMaintenance = command.lastMaintenance();
        this.nextMaintenance = command.nextMaintenance();
        this.totalKilometers = command.totalKilometers();
        this.rating = command.rating();
        return this;
    }
}