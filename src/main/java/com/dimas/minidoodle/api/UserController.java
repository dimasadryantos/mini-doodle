package com.dimas.minidoodle.api;

import com.dimas.minidoodle.api.dto.CreateUserRequest;
import com.dimas.minidoodle.api.dto.UserResponse;
import com.dimas.minidoodle.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a user (a personal calendar is created implicitly)")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.create(request.name(), request.email()));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get a user")
    public UserResponse get(@PathVariable UUID userId) {
        return UserResponse.from(userService.get(userId));
    }
}
