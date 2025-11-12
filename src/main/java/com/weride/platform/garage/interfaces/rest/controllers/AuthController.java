package com.weride.platform.garage.interfaces.rest.controllers;

import com.weride.platform.garage.application.internal.commandservices.RegisterUserCommandService;
import com.weride.platform.garage.domain.model.aggregates.User;
import com.weride.platform.garage.domain.model.commands.RegisterUserCommand;
import com.weride.platform.garage.infrastructure.configuration.JwtUtil;
import com.weride.platform.garage.infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticación de usuarios")
@RequiredArgsConstructor
public class AuthController {
    private final RegisterUserCommandService registerUserCommandService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public User register(@RequestBody RegisterUserCommand command) {
        return registerUserCommandService.handle(command);
    }


    @PostMapping("/login")
    @Operation(
            summary = "Login de usuario",
            description = "Valida credenciales y devuelve un JWT para acceder a los endpoints protegidos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login exitoso, devuelve JWT"),
                    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
            }
    )
    public String login(@RequestBody User user) {
        User found = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (found.getPassword().equals(user.getPassword())) {
            return jwtUtil.generateToken(found.getUsername());
        }
        throw new RuntimeException("Invalid credentials");
    }
}
