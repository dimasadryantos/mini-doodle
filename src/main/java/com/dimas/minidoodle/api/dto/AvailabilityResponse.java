package com.dimas.minidoodle.api.dto;

import com.dimas.minidoodle.domain.SlotStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Aggregated free/busy view of a user's calendar over a time frame. */
public record AvailabilityResponse(
        UUID userId,
        Instant from,
        Instant to,
        List<Interval> intervals,
        long freeMinutes,
        long busyMinutes) {

    /** A maximal run of adjacent slots sharing the same status, clipped to [from, to). */
    public record Interval(Instant start, Instant end, SlotStatus status, int slotCount) {
    }
}
