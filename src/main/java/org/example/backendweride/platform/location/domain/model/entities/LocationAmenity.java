package org.example.backendweride.platform.location.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backendweride.platform.location.domain.model.aggregates.Location;

@Entity
@Table(name = "location_amenities")
@Getter
@NoArgsConstructor
public class LocationAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "amenity", nullable = false, length = 255)
    private String amenity;

    public LocationAmenity(Location location, String amenity) {
        this.location = location;
        this.amenity = amenity;
    }
}
