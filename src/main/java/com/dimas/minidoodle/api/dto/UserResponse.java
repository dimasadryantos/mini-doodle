package com.dimas.minidoodle.api.dto;

import com.dimas.minidoodle.domain.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, Instant createdAt) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getCreatedAt());
    }
}
