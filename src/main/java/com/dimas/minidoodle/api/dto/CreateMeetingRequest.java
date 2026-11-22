package com.dimas.minidoodle.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record CreateMeetingRequest(
        @NotNull UUID slotId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 4000) String description,
        @Size(max = 100) Set<@Email @NotBlank String> participants) {
}
