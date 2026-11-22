package com.dimas.minidoodle.repository;

import com.dimas.minidoodle.domain.SlotStatus;
import com.dimas.minidoodle.domain.TimeSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    Optional<TimeSlot> findByIdAndCalendarId(UUID id, UUID calendarId);

    @Query("""
            select s from TimeSlot s
            where s.calendar.id = :calendarId
              and s.startTime < :to and s.endTime > :from
            order by s.startTime
            """)
    Page<TimeSlot> findInRange(@Param("calendarId") UUID calendarId,
                               @Param("from") Instant from,
                               @Param("to") Instant to,
                               Pageable pageable);

    @Query("""
            select s from TimeSlot s
            where s.calendar.id = :calendarId
              and s.status = :status
              and s.startTime < :to and s.endTime > :from
            order by s.startTime
            """)
    Page<TimeSlot> findInRangeWithStatus(@Param("calendarId") UUID calendarId,
                                         @Param("from") Instant from,
                                         @Param("to") Instant to,
                                         @Param("status") SlotStatus status,
                                         Pageable pageable);

    @Query("""
            select s from TimeSlot s
            where s.calendar.id = :calendarId
              and s.startTime < :to and s.endTime > :from
            order by s.startTime
            """)
    List<TimeSlot> findAllInRange(@Param("calendarId") UUID calendarId,
                                  @Param("from") Instant from,
                                  @Param("to") Instant to);
}
