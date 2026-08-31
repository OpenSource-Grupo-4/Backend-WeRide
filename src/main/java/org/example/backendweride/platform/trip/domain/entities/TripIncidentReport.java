package org.example.backendweride.platform.trip.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backendweride.platform.trip.domain.aggregates.Trip;

@Entity
@Table(name = "trip_incident_reports")
@Getter
@NoArgsConstructor
public class TripIncidentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "incident_report", nullable = false, length = 255)
    private String incidentReport;

    public TripIncidentReport(Trip trip, String incidentReport) {
        this.trip = trip;
        this.incidentReport = incidentReport;
    }
}
