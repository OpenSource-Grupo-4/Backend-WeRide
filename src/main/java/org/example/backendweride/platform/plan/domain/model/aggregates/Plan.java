package org.example.backendweride.platform.plan.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.backendweride.platform.plan.domain.commands.CreatePlanCommand;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    private String name;
    @Getter
    private String description;
    @Getter
    float price;
    @Getter
    String currency;
    @Getter
    float pricePerMinute;
    @Getter
    String duration;
    @Getter
    int  durationDays;
    @Getter
    int maxTripsPerDay;
    @Getter
    int maxMinutesPerTrip;
    @Getter
    int freeMinutesPerMonth;
    @Getter
    int discountPercentage;
    @ElementCollection
    @CollectionTable(name = "plan_benefits", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "benefit")
    @Getter
    List<String> benefits = new ArrayList<>();
    @Getter
    String color;
    @Getter
    boolean isPopular;
    @Getter
    boolean isActive;

    protected Plan() {

    }
    public Plan(CreatePlanCommand planCommand) {
        this.name = planCommand.name();
        this.description = planCommand.description();
        this.price = planCommand.price();
        this.currency = planCommand.currency();
        this.pricePerMinute = planCommand.pricePerMinute();
        this.duration = planCommand.duration();
        this.durationDays = planCommand.durationDays();
        this.maxTripsPerDay = planCommand.maxTripsPerDay();
        this.maxMinutesPerTrip = planCommand.maxMinutesPerTrip();
        this.freeMinutesPerMonth = planCommand.freeMinutesPerMonth();
        this.discountPercentage = planCommand.discountPercentage();
        this.benefits = planCommand.benefits();
        this.color = planCommand.color();
        this.isPopular = planCommand.isPopular();
        this.isActive = planCommand.isActive();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plan other = (Plan) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
