package com.dimas.minidoodle.service;

import com.dimas.minidoodle.api.dto.AvailabilityResponse.Interval;
import com.dimas.minidoodle.domain.SlotStatus;
import com.dimas.minidoodle.domain.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityMergeTest {

    private static final Instant T0 = Instant.parse("2026-01-05T09:00:00Z");

    private static TimeSlot slot(int startMin, int endMin, SlotStatus status) {
        TimeSlot s = new TimeSlot(null, T0.plusSeconds(startMin * 60L), T0.plusSeconds(endMin * 60L));
        if (status == SlotStatus.BUSY) s.markBusy();
        return s;
    }

    @Test
    void mergesAdjacentSlotsWithSameStatus() {
        List<Interval> out = AvailabilityService.merge(List.of(
                slot(0, 30, SlotStatus.FREE),
                slot(30, 60, SlotStatus.FREE),
                slot(60, 90, SlotStatus.BUSY),
                slot(90, 120, SlotStatus.FREE)),
                T0, T0.plusSeconds(3600 * 3));

        assertThat(out).hasSize(3);
        assertThat(out.get(0).status()).isEqualTo(SlotStatus.FREE);
        assertThat(out.get(0).slotCount()).isEqualTo(2);
        assertThat(out.get(0).end()).isEqualTo(T0.plusSeconds(3600));
        assertThat(out.get(1).status()).isEqualTo(SlotStatus.BUSY);
        assertThat(out.get(2).start()).isEqualTo(T0.plusSeconds(90 * 60));
    }

    @Test
    void doesNotMergeAcrossGaps() {
        List<Interval> out = AvailabilityService.merge(List.of(
                slot(0, 30, SlotStatus.FREE),
                slot(45, 75, SlotStatus.FREE)),
                T0, T0.plusSeconds(7200));

        assertThat(out).hasSize(2);
    }

    @Test
    void clipsToRequestedFrame() {
        List<Interval> out = AvailabilityService.merge(List.of(slot(0, 120, SlotStatus.BUSY)),
                T0.plusSeconds(1800), T0.plusSeconds(3600));

        assertThat(out).hasSize(1);
        assertThat(out.get(0).start()).isEqualTo(T0.plusSeconds(1800));
        assertThat(out.get(0).end()).isEqualTo(T0.plusSeconds(3600));
    }

    @Test
    void emptyInputYieldsEmptyOutput() {
        assertThat(AvailabilityService.merge(List.of(), T0, T0.plusSeconds(60))).isEmpty();
    }
}
