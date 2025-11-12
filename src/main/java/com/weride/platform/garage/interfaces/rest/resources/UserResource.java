package com.weride.platform.garage.interfaces.rest.resources;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResource {
    private Long id;
    private String username;
    private String email;
}
