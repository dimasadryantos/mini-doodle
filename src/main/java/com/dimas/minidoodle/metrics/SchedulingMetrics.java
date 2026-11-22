package com.dimas.minidoodle.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class SchedulingMetrics {

    private final Counter slotsCreated;
    private final Counter meetingsBooked;
    private final Counter meetingsCancelled;
    private final Counter bookingConflicts;
    private final Timer availabilityQuery;

    public SchedulingMetrics(MeterRegistry registry) {
        this.slotsCreated = Counter.builder("minidoodle.slots.added")
                .description("Number of time slots created").register(registry);
        this.meetingsBooked = Counter.builder("minidoodle.meetings.booked")
                .description("Number of meetings booked").register(registry);
        this.meetingsCancelled = Counter.builder("minidoodle.meetings.cancelled")
                .description("Number of meetings cancelled").register(registry);
        this.bookingConflicts = Counter.builder("minidoodle.meetings.conflicts")
                .description("Booking attempts rejected because the slot was not free").register(registry);
        this.availabilityQuery = Timer.builder("minidoodle.availability.query")
                .description("Latency of aggregated availability queries")
                .publishPercentiles(0.5, 0.95, 0.99).register(registry);
    }

    public void slotCreated() { slotsCreated.increment(); }
    public void meetingBooked() { meetingsBooked.increment(); }
    public void meetingCancelled() { meetingsCancelled.increment(); }
    public void bookingConflict() { bookingConflicts.increment(); }

    public <T> T timeAvailability(Supplier<T> query) {
        return availabilityQuery.record(query);
    }
}