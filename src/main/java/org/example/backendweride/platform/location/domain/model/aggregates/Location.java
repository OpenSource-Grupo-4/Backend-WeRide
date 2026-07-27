package org.example.backendweride.platform.location.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.backendweride.platform.location.domain.commands.CreateLocationCommand;
import org.example.backendweride.platform.location.domain.valueobjects.Coordinates;
import org.example.backendweride.platform.location.domain.valueobjects.OperatingHours;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    @ElementCollection
    @CollectionTable(name = "location_amenities", joinColumns = @JoinColumn(name = "location_id"))
    @Column(name = "amenity")
    @Getter
    private List<String> amenities = new ArrayList<>();
    @Getter
    private String district;

    @Column(columnDefinition = "TEXT")
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
        this.amenities = command.amenities();
        this.district = command.district();
        this.description = command.description();
        this.image = command.image();
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
