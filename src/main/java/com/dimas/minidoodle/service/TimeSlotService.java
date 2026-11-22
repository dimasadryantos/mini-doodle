package com.dimas.minidoodle.service;

import com.dimas.minidoodle.domain.Calendar;
import com.dimas.minidoodle.domain.SlotStatus;
import com.dimas.minidoodle.domain.TimeSlot;
import com.dimas.minidoodle.metrics.SchedulingMetrics;
import com.dimas.minidoodle.repository.CalendarRepository;
import com.dimas.minidoodle.repository.TimeSlotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final CalendarRepository calendarRepository;
    private final SchedulingMetrics schedulingMetrics;

    public TimeSlotService(TimeSlotRepository timeSlotRepository, CalendarRepository calendarRepository, SchedulingMetrics schedulingMetrics) {
        this.timeSlotRepository = timeSlotRepository;
        this.calendarRepository = calendarRepository;
        this.schedulingMetrics = schedulingMetrics;
    }

    @Transactional
    public TimeSlot create(UUID userId, Instant start, int durationMinutes) {
        Calendar calendar = calendarOf(userId);
        Instant end = start.plus(Duration.ofMinutes(durationMinutes));
        TimeSlot saved = timeSlotRepository.saveAndFlush(new TimeSlot(calendar, start, end));
        schedulingMetrics.slotCreated();
        return saved;
    }

    @Transactional(readOnly = true)
    public TimeSlot get(UUID userId, UUID slotId) {
        return timeSlotRepository.findByIdAndCalendarId(slotId, calendarOf(userId).getId())
                .orElseThrow(() -> new NotFoundException("Slot " + slotId + " not found for user " + userId));
    }

    @Transactional(readOnly = true)
    public Page<TimeSlot> list(UUID userId, Instant from, Instant to, SlotStatus status, Pageable pageable) {
        validateRange(from, to);
        UUID calendarId = calendarOf(userId).getId();
        return status == null
                ? timeSlotRepository.findInRange(calendarId, from, to, pageable)
                : timeSlotRepository.findInRangeWithStatus(calendarId, from, to, status, pageable);
    }

    @Transactional
    public TimeSlot reschedule(UUID userId, UUID slotId, Instant start, int durationMinutes) {
        TimeSlot slot = get(userId, slotId);
        slot.reschedule(start, start.plus(Duration.ofMinutes(durationMinutes)));
        return timeSlotRepository.saveAndFlush(slot);
    }

    @Transactional
    public TimeSlot updateStatus(UUID userId, UUID slotId, SlotStatus status) {
        TimeSlot slot = get(userId, slotId);
        if (status == SlotStatus.FREE && slot.isBooked()) {
            throw new ConflictException("Slot " + slotId + " has a meeting; cancel the meeting to free it");
        }
        if (status == SlotStatus.FREE) {
            slot.markFree();
        } else {
            slot.markBusy();
        }
        return slot;
    }

    @Transactional
    public void delete(UUID userId, UUID slotId) {
        TimeSlot slot = get(userId, slotId);
        if (slot.isBooked()) {
            throw new ConflictException("Slot " + slotId + " has a meeting; cancel the meeting first");
        }
        timeSlotRepository.delete(slot);
    }

    Calendar calendarOf(UUID userId) {
        return calendarRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
    }

    static void validateRange(Instant from, Instant to) {
        if (from == null || to == null || !to.isAfter(from)) {
            throw new IllegalArgumentException("'to' must be after 'from'");
        }
        if (Duration.between(from, to).toDays() > 366) {
            throw new IllegalArgumentException("Time frame must not exceed one year");
        }
    }
}
