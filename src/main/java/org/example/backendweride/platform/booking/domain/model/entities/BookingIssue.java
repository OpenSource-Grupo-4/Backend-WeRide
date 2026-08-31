package org.example.backendweride.platform.booking.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;

@Entity
@Table(name = "booking_issues")
@Getter
@NoArgsConstructor
public class BookingIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "issue", nullable = false, length = 255)
    private String issue;

    public BookingIssue(Booking booking, String issue) {
        this.booking = booking;
        this.issue = issue;
    }
}
