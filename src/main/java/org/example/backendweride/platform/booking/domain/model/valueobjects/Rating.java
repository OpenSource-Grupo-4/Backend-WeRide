package org.example.backendweride.platform.booking.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.Getter;

/**
 * Rating value object
 * Represents the rating of a booking
 */
@Embeddable
@Getter
public class Rating {

    private Integer score;
    private String comment;

    public Rating() {
    }

    public Rating(Integer score, String comment) {
        this.score = score;
        this.comment = comment;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

