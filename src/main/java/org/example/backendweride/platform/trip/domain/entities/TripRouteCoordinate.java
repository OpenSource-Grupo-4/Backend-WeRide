package org.example.backendweride.platform.trip.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backendweride.platform.trip.domain.aggregates.Trip;

@Entity
@Table(name = "trip_route_coordinates")
@Getter
@NoArgsConstructor
public class TripRouteCoordinate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "lat", nullable = false)
    private double lat;

    @Column(name = "lng", nullable = false)
    private double lng;

    public TripRouteCoordinate(Trip trip, double lat, double lng) {
        this.trip = trip;
        this.lat = lat;
        this.lng = lng;
    }
}
