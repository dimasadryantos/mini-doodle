package com.dimas.minidoodle.service;

import com.dimas.minidoodle.api.dto.AvailabilityResponse;
import com.dimas.minidoodle.api.dto.AvailabilityResponse.Interval;
import com.dimas.minidoodle.domain.SlotStatus;
import com.dimas.minidoodle.domain.TimeSlot;
import com.dimas.minidoodle.metrics.SchedulingMetrics;
import com.dimas.minidoodle.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {

    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotService slotService;
    private final SchedulingMetrics schedulingMetrics;

    public AvailabilityService(TimeSlotRepository timeSlotRepository, TimeSlotService slotService, SchedulingMetrics schedulingMetrics) {
        this.timeSlotRepository = timeSlotRepository;
        this.slotService = slotService;
        this.schedulingMetrics = schedulingMetrics;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse availability(UUID userId, Instant from, Instant to) {
        TimeSlotService.validateRange(from, to);
        UUID calendarId = slotService.calendarOf(userId).getId();
        List<TimeSlot> inRange = schedulingMetrics.timeAvailability(() -> timeSlotRepository.findAllInRange(calendarId, from, to));
        List<Interval> intervals = merge(inRange, from, to);

        long free = 0, busy = 0;
        for (Interval i : intervals) {
            long minutes = Duration.between(i.start(), i.end()).toMinutes();
            if (i.status() == SlotStatus.FREE) free += minutes; else busy += minutes;
        }
        return new AvailabilityResponse(userId, from, to, intervals, free, busy);
    }

    static List<Interval> merge(List<TimeSlot> sorted, Instant from, Instant to) {
        List<Interval> out = new ArrayList<>();
        Instant curStart = null, curEnd = null;
        SlotStatus curStatus = null;
        int count = 0;

        for (TimeSlot s : sorted) {
            Instant start = s.getStartTime().isBefore(from) ? from : s.getStartTime();
            Instant end = s.getEndTime().isAfter(to) ? to : s.getEndTime();
            if (!end.isAfter(start)) {
                continue;
            }
            boolean adjacent = curEnd != null && curEnd.equals(start) && curStatus == s.getStatus();
            if (adjacent) {
                curEnd = end;
                count++;
            } else {
                if (curStart != null) {
                    out.add(new Interval(curStart, curEnd, curStatus, count));
                }
                curStart = start;
                curEnd = end;
                curStatus = s.getStatus();
                count = 1;
            }
        }
        if (curStart != null) {
            out.add(new Interval(curStart, curEnd, curStatus, count));
        }
        return out;
    }
}
