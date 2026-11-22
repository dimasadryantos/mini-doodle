package com.dimas.minidoodle.api;

import com.dimas.minidoodle.AbstractIntegrationTest;
import com.dimas.minidoodle.api.dto.*;
import com.dimas.minidoodle.domain.SlotStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulingFlowIT extends AbstractIntegrationTest {

    private static final Instant DAY = Instant.parse("2026-03-02T09:00:00Z");

    private UUID userId;

    @BeforeEach
    void createUser() {
        var res = rest.postForEntity("/api/users",
                new CreateUserRequest("Dimas", UUID.randomUUID() + "@example.com"), UserResponse.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        userId = res.getBody().id();
    }

    @Test
    void createListUpdateAndDeleteSlots() {
        SlotResponse slot = createSlot(DAY, 30);
        assertThat(slot.status()).isEqualTo(SlotStatus.FREE);
        assertThat(slot.endTime()).isEqualTo(DAY.plusSeconds(1800));

        var updated = rest.exchange(slotUrl(slot.id()), org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(new UpdateSlotRequest(DAY.plusSeconds(3600), 45)),
                SlotResponse.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().startTime()).isEqualTo(DAY.plusSeconds(3600));

        var busy = rest.exchange(slotUrl(slot.id()) + "/status", org.springframework.http.HttpMethod.PATCH,
                new org.springframework.http.HttpEntity<>(new UpdateSlotStatusRequest(SlotStatus.BUSY)),
                SlotResponse.class);
        assertThat(busy.getBody().status()).isEqualTo(SlotStatus.BUSY);

        JsonNode page = rest.getForObject(listUrl(DAY, DAY.plusSeconds(86400)) + "&status=BUSY", JsonNode.class);
        assertThat(page.get("totalElements").asLong()).isEqualTo(1);

        rest.delete(slotUrl(slot.id()));
        var gone = rest.getForEntity(slotUrl(slot.id()), ProblemDetail.class);
        assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void overlappingSlotIsRejected() {
        createSlot(DAY, 60);
        var res = rest.postForEntity(slotsUrl(), new CreateSlotRequest(DAY.plusSeconds(1800), 60), ProblemDetail.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res.getBody().getDetail()).contains("overlaps");
    }

    @Test
    void bookingLifecycle() {
        SlotResponse slot = createSlot(DAY, 30);

        var booked = rest.postForEntity(meetingsUrl(),
                new CreateMeetingRequest(slot.id(), "Design review", "Discuss API", Set.of("a@x.com", "b@x.com")),
                MeetingResponse.class);
        assertThat(booked.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(booked.getBody().participants()).containsExactlyInAnyOrder("a@x.com", "b@x.com");

        // slot is now busy and linked to the meeting
        SlotResponse after = rest.getForObject(slotUrl(slot.id()), SlotResponse.class);
        assertThat(after.status()).isEqualTo(SlotStatus.BUSY);
        assertThat(after.meetingId()).isEqualTo(booked.getBody().id());

        // a second booking of the same slot is rejected
        var again = rest.postForEntity(meetingsUrl(),
                new CreateMeetingRequest(slot.id(), "Other", null, Set.of()), ProblemDetail.class);
        assertThat(again.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // cannot free or delete a booked slot
        var free = rest.exchange(slotUrl(slot.id()) + "/status", org.springframework.http.HttpMethod.PATCH,
                new org.springframework.http.HttpEntity<>(new UpdateSlotStatusRequest(SlotStatus.FREE)), ProblemDetail.class);
        assertThat(free.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        var del = rest.exchange(slotUrl(slot.id()), org.springframework.http.HttpMethod.DELETE, null, ProblemDetail.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // cancelling frees the slot
        rest.delete(meetingsUrl() + "/" + booked.getBody().id());
        assertThat(rest.getForObject(slotUrl(slot.id()), SlotResponse.class).status()).isEqualTo(SlotStatus.FREE);
    }

    @Test
    void availabilityAggregatesAdjacentSlots() {
        SlotResponse s1 = createSlot(DAY, 30);
        createSlot(DAY.plusSeconds(1800), 30);
        createSlot(DAY.plusSeconds(3600), 30);
        rest.postForEntity(meetingsUrl(), new CreateMeetingRequest(s1.id(), "Standup", null, Set.of()), MeetingResponse.class);

        AvailabilityResponse av = rest.getForObject(
                "/api/users/" + userId + "/availability?from=" + DAY + "&to=" + DAY.plusSeconds(7200),
                AvailabilityResponse.class);

        assertThat(av.intervals()).hasSize(2);
        assertThat(av.intervals().get(0).status()).isEqualTo(SlotStatus.BUSY);
        assertThat(av.intervals().get(1).status()).isEqualTo(SlotStatus.FREE);
        assertThat(av.intervals().get(1).slotCount()).isEqualTo(2);
        assertThat(av.busyMinutes()).isEqualTo(30);
        assertThat(av.freeMinutes()).isEqualTo(60);
    }

    @Test
    void validationErrorsReturn400() {
        var res = rest.postForEntity(slotsUrl(), Map.of("startTime", DAY.toString(), "durationMinutes", 1), ProblemDetail.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        var badRange = rest.getForEntity(listUrl(DAY, DAY.minusSeconds(60)), ProblemDetail.class);
        assertThat(badRange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private SlotResponse createSlot(Instant start, int minutes) {
        ResponseEntity<SlotResponse> res = rest.postForEntity(slotsUrl(), new CreateSlotRequest(start, minutes), SlotResponse.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return res.getBody();
    }

    private String slotsUrl() { return "/api/users/" + userId + "/slots"; }
    private String slotUrl(UUID id) { return slotsUrl() + "/" + id; }
    private String meetingsUrl() { return "/api/users/" + userId + "/meetings"; }
    private String listUrl(Instant from, Instant to) { return slotsUrl() + "?from=" + from + "&to=" + to; }
}
