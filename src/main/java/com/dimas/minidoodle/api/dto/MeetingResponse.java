package com.dimas.minidoodle.api.dto;

import com.dimas.minidoodle.domain.Meeting;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record MeetingResponse(
        UUID id,
        UUID slotId,
        Instant startTime,
        Instant endTime,
        String title,
        String description,
        Set<String> participants,
        Instant createdAt) {

    public static MeetingResponse from(Meeting m) {
        return new MeetingResponse(
                m.getId(), m.getSlot().getId(),
                m.getSlot().getStartTime(), m.getSlot().getEndTime(),
                m.getTitle(), m.getDescription(), m.getParticipants(), m.getCreatedAt());
    }
}
