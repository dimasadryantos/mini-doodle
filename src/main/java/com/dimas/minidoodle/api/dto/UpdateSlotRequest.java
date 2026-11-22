package com.dimas.minidoodle.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateSlotRequest(
        @NotNull Instant startTime,
        @NotNull @Min(5) @Max(24 * 60) Integer durationMinutes) {
}
