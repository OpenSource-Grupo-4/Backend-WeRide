package org.example.backendweride.platform.garage.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Password {
    private String value;
}
