package com.dimas.minidoodle.repository;

import com.dimas.minidoodle.domain.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CalendarRepository extends JpaRepository<Calendar, UUID> {
    Optional<Calendar> findByUserId(UUID userId);
}
