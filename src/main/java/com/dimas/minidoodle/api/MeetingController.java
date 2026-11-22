package com.dimas.minidoodle.api;

import com.dimas.minidoodle.api.dto.CreateMeetingRequest;
import com.dimas.minidoodle.api.dto.MeetingResponse;
import com.dimas.minidoodle.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/meetings")
@Tag(name = "Meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Book a FREE slot as a meeting with title, description and participants")
    public MeetingResponse book(@PathVariable UUID userId, @Valid @RequestBody CreateMeetingRequest req) {
        return MeetingResponse.from(meetingService.book(
                userId, req.slotId(), req.title(), req.description(), req.participants()));
    }

    @GetMapping("/{meetingId}")
    @Operation(summary = "Get a meeting")
    public MeetingResponse get(@PathVariable UUID userId, @PathVariable UUID meetingId) {
        return MeetingResponse.from(meetingService.get(userId, meetingId));
    }

    @DeleteMapping("/{meetingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel a meeting; its slot becomes FREE again")
    public void cancel(@PathVariable UUID userId, @PathVariable UUID meetingId) {
        meetingService.cancel(userId, meetingId);
    }
}
