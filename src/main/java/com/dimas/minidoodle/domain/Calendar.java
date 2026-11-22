package com.dimas.minidoodle.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "calendars")
public class Calendar {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 64)
    private String timezone = "UTC";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Calendar() {
    }

    Calendar(User user) {
        this.user = user;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getTimezone() { return timezone; }
}
