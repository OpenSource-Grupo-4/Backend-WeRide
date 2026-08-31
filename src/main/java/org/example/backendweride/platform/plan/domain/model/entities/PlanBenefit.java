package org.example.backendweride.platform.plan.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backendweride.platform.plan.domain.model.aggregates.Plan;

@Entity
@Table(name = "plan_benefits")
@Getter
@NoArgsConstructor
public class PlanBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "benefit", nullable = false, length = 255)
    private String benefit;

    public PlanBenefit(Plan plan, String benefit) {
        this.plan = plan;
        this.benefit = benefit;
    }
}
