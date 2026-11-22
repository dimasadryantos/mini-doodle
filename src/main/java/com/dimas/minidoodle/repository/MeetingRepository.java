package com.dimas.minidoodle.repository;

import com.dimas.minidoodle.domain.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
}
