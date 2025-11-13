package org.example.backendweride.platform.garage.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100)
    private String username;

    @Column(nullable=false, unique=true, length=150)
    private String email;

    @Column(nullable=false, length=255)
    private String password;
}
