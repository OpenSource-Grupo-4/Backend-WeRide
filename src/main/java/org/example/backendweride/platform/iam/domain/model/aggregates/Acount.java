package org.example.backendweride.platform.iam.domain.model.aggregates;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backendweride.platform.iam.domain.model.commands.SignUpCommand;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Acount Aggregate Root
 *
 * @summary Represents an account in WeRide Platform.
 */
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Acount extends AbstractAggregateRoot<Acount> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @NotBlank
    @Getter
    @Size(max=20)
    @Column(unique = true)
    private String userName;

    @NotBlank
    @Getter
    private String password;

    public Acount(SignUpCommand command) {}

    public Acount(SignUpCommand command, String hashedPassword) {}

}
