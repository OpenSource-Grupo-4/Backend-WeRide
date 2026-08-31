package org.example.backendweride.platform.location.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.backendweride.platform.location.domain.commands.CreateLocationCommand;
import org.example.backendweride.platform.location.domain.model.entities.LocationAmenity;
import org.example.backendweride.platform.location.domain.valueobjects.Coordinates;
import org.example.backendweride.platform.location.domain.valueobjects.OperatingHours;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    private String name;
    @Getter
    private String address;

    @Embedded
    @Getter
    private Coordinates coordinates;

    @Getter
    private String type;
    @Getter
    private int capacity;
    @Getter
    private int availableSpots;
    @Getter
    private boolean isActive;

    @Embedded
    @Getter
    private OperatingHours operatingHours;
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    List<LocationAmenity> amenityEntities = new ArrayList<>();

    public List<String> getAmenities() {
        return amenityEntities.stream().map(LocationAmenity::getAmenity).collect(Collectors.toList());
    }
    @Getter
    private String district;

    @Column(length = 1000)
    @Getter
    private String description;
    @Getter
    private String image;

    protected Location() {
    }

    public Location(CreateLocationCommand locationCommand){
        updateFrom(locationCommand);
    }

    public void updateFrom(CreateLocationCommand command) {
        this.name = command.name();
        this.address = command.address();
        this.coordinates = command.coordinates();
        this.type = command.type();
        this.capacity = command.capacity();
        this.availableSpots = command.availableSpots();
        this.isActive = command.isActive();
        this.operatingHours = command.operatingHours();
        this.amenityEntities = toAmenityEntities(command.amenities());
        this.district = command.district();
        this.description = command.description();
        this.image = command.image();
    }

    private List<LocationAmenity> toAmenityEntities(List<String> amenities) {
        if (amenities == null) return new ArrayList<>();
        return amenities.stream().map(amenity -> new LocationAmenity(this, amenity)).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location other = (Location) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
