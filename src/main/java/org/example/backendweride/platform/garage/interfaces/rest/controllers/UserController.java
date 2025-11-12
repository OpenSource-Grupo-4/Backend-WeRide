package org.example.backendweride.platform.garage.interfaces.rest.controllers;

import org.example.backendweride.platform.garage.application.internal.queryservices.UserQueryServiceImpl;
import org.example.backendweride.platform.garage.domain.model.aggregates.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserQueryServiceImpl userQueryService;

    @GetMapping
    public List<User> getAllUsers() { return userQueryService.handle(); }
}
