package com.dimas.minidoodle.api.dto;

import com.dimas.minidoodle.domain.SlotStatus;
import com.dimas.minidoodle.domain.TimeSlot;

import java.time.Instant;
import java.util.UUID;

public record SlotResponse(
        UUID id,
        Instant startTime,
        Instant endTime,
        SlotStatus status,
        UUID meetingId,
        long version) {

    public static SlotResponse from(TimeSlot s) {
        return new SlotResponse(
                s.getId(), s.getStartTime(), s.getEndTime(), s.getStatus(),
                s.getMeeting() == null ? null : s.getMeeting().getId(),
                s.getVersion());
    }
}