package org.example.backendweride.platform.profile.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.backendweride.platform.profile.domain.model.commands.CreateProfileCommand;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private Long id;
    @Getter
    private Long userId;
    @Getter
    private String firstName;
    @Getter
    private String lastName;
    @Getter
    private String email;

    protected Profile() {}

    public Profile(CreateProfileCommand profileCommand) {
        this.userId = profileCommand.userId();
        this.firstName = "";
        this.lastName = "";
        this.email = "";
    }
}
