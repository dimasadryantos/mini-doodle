package com.dimas.minidoodle.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "time_slots")
public class TimeSlot {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SlotStatus status = SlotStatus.FREE;

    /** Optimistic lock: two users booking the same slot cannot both win. */
    @Version
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToOne(mappedBy = "slot", fetch = FetchType.LAZY)
    private Meeting meeting;

    protected TimeSlot() {
    }

    public TimeSlot(Calendar calendar, Instant startTime, Instant endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        this.calendar = calendar;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void reschedule(Instant newStart, Instant newEnd) {
        if (!newEnd.isAfter(newStart)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        this.startTime = newStart;
        this.endTime = newEnd;
        touch();
    }

    public void markBusy() {
        this.status = SlotStatus.BUSY;
        touch();
    }

    public void markFree() {
        this.status = SlotStatus.FREE;
        touch();
    }

    /** Called by {@link Meeting} when the slot is booked. */
    void attach(Meeting meeting) {
        this.meeting = meeting;
        markBusy();
    }

    /** Drops the meeting reference and frees the slot (meeting cancellation). */
    public void release() {
        this.meeting = null;
        markFree();
    }

    public boolean isFree() {
        return status == SlotStatus.FREE;
    }

    public boolean isBooked() {
        return meeting != null;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Calendar getCalendar() { return calendar; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public SlotStatus getStatus() { return status; }
    public long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Meeting getMeeting() { return meeting; }
}
