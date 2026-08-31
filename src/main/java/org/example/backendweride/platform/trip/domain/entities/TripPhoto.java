package org.example.backendweride.platform.trip.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backendweride.platform.trip.domain.aggregates.Trip;

@Entity
@Table(name = "trip_photos")
@Getter
@NoArgsConstructor
public class TripPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "photo", nullable = false, length = 255)
    private String photo;

    public TripPhoto(Trip trip, String photo) {
        this.trip = trip;
        this.photo = photo;
    }
}
