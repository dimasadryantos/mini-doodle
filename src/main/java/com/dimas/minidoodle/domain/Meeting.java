package com.dimas.minidoodle.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private TimeSlot slot;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "meeting_participants", joinColumns = @JoinColumn(name = "meeting_id"))
    @Column(name = "email", nullable = false)
    private Set<String> participants = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Meeting() {
    }

    public Meeting(TimeSlot slot, String title, String description, Set<String> participants) {
        this.slot = slot;
        this.title = title;
        this.description = description;
        this.participants = new LinkedHashSet<>(participants);
        slot.attach(this);
    }

    public UUID getId() { return id; }
    public TimeSlot getSlot() { return slot; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Set<String> getParticipants() { return participants; }
    public Instant getCreatedAt() { return createdAt; }
}
