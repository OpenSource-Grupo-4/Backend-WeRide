package com.weride.platform.garage.interfaces.rest.controllers;

import com.weride.platform.garage.application.internal.queryservices.UserQueryServiceImpl;
import com.weride.platform.garage.domain.model.aggregates.User;
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
