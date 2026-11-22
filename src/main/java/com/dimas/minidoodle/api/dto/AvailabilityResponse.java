package com.dimas.minidoodle.api.dto;

import com.dimas.minidoodle.domain.SlotStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
        UUID userId,
        Instant from,
        Instant to,
        List<Interval> intervals,
        long freeMinutes,
        long busyMinutes) {

    public record Interval(Instant start, Instant end, SlotStatus status, int slotCount) {
    }
}
