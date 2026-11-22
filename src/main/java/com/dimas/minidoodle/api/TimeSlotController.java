package com.dimas.minidoodle.api;

import com.dimas.minidoodle.api.dto.*;
import com.dimas.minidoodle.domain.SlotStatus;
import com.dimas.minidoodle.service.TimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/slots")
@Tag(name = "Time slots")
public class TimeSlotController {

    private static final int MAX_PAGE_SIZE = 500;
    private final TimeSlotService slotService;

    public TimeSlotController(TimeSlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an available slot with a configurable duration")
    public SlotResponse create(@PathVariable UUID userId, @Valid @RequestBody CreateSlotRequest req) {
        return SlotResponse.from(slotService.create(userId, req.startTime(), req.durationMinutes()));
    }

    @GetMapping
    @Operation(summary = "List slots intersecting a time frame, optionally filtered by status")
    public Page<SlotResponse> list(@PathVariable UUID userId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                   @RequestParam(required = false) SlotStatus status,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        return slotService.list(userId, from, to, status, pageable).map(SlotResponse::from);
    }

    @GetMapping("/{slotId}")
    @Operation(summary = "Get a single slot")
    public SlotResponse get(@PathVariable UUID userId, @PathVariable UUID slotId) {
        return SlotResponse.from(slotService.get(userId, slotId));
    }

    @PutMapping("/{slotId}")
    @Operation(summary = "Reschedule a slot (new start and duration)")
    public SlotResponse update(@PathVariable UUID userId, @PathVariable UUID slotId,
                               @Valid @RequestBody UpdateSlotRequest req) {
        return SlotResponse.from(slotService.reschedule(userId, slotId, req.startTime(), req.durationMinutes()));
    }

    @PatchMapping("/{slotId}/status")
    @Operation(summary = "Mark a slot FREE or BUSY")
    public SlotResponse updateStatus(@PathVariable UUID userId, @PathVariable UUID slotId,
                                     @Valid @RequestBody UpdateSlotStatusRequest req) {
        return SlotResponse.from(slotService.updateStatus(userId, slotId, req.status()));
    }

    @DeleteMapping("/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a slot (must not have a meeting)")
    public void delete(@PathVariable UUID userId, @PathVariable UUID slotId) {
        slotService.delete(userId, slotId);
    }
}