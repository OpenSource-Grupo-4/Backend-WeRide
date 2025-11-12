package com.weride.platform.garage.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Money {
    private Double amount;
    private String currency;
}
