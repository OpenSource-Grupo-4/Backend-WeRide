package org.example.backendweride.platform.garage.domain.model.commands;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterUserCommand {
    private String username;
    private String email;
    private String password;
}
