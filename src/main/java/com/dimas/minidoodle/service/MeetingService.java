package com.dimas.minidoodle.service;

import com.dimas.minidoodle.domain.Meeting;
import com.dimas.minidoodle.domain.TimeSlot;
import com.dimas.minidoodle.metrics.SchedulingMetrics;
import com.dimas.minidoodle.repository.MeetingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class MeetingService {

    private final MeetingRepository meetings;
    private final TimeSlotService slotService;
    private final SchedulingMetrics metrics;

    public MeetingService(MeetingRepository meetings, TimeSlotService slotService, SchedulingMetrics metrics) {
        this.meetings = meetings;
        this.slotService = slotService;
        this.metrics = metrics;
    }

    @Transactional
    public Meeting book(UUID userId, UUID slotId, String title, String description, Set<String> participants) {
        TimeSlot slot = slotService.get(userId, slotId);
        if (!slot.isFree()) {
            metrics.bookingConflict();
            throw new ConflictException("Slot " + slotId + " is not free");
        }
        Meeting meeting = new Meeting(slot, title, description, participants == null ? Set.of() : participants);
        Meeting saved = meetings.saveAndFlush(meeting);
        metrics.meetingBooked();
        return saved;
    }

    @Transactional(readOnly = true)
    public Meeting get(UUID userId, UUID meetingId) {
        Meeting meeting = meetings.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("Meeting " + meetingId + " not found"));
        if (!meeting.getSlot().getCalendar().getUser().getId().equals(userId)) {
            throw new NotFoundException("Meeting " + meetingId + " not found for user " + userId);
        }
        return meeting;
    }

    @Transactional
    public void cancel(UUID userId, UUID meetingId) {
        Meeting meeting = get(userId, meetingId);
        meeting.getSlot().release();
        meetings.delete(meeting);
        metrics.meetingCancelled();
    }
}
