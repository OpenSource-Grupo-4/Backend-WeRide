package org.example.backendweride.platform.profile.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.backendweride.platform.profile.domain.model.commands.CreateProfileCommand;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Objects;

@Entity
@Table(name = "profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_profile_user", columnNames = {"user_id"})
})
@EntityListeners(AuditingEntityListener.class)
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;
    @Getter
    @Column(nullable = false)
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
        this.firstName = profileCommand.firstName();
        this.lastName = profileCommand.lastName();
        this.email = profileCommand.email();
    }

    public void update(String firstName, String lastName, String email) {
        if (firstName != null) this.firstName = firstName;
        if (lastName != null) this.lastName = lastName;
        if (email != null) this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile other = (Profile) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
