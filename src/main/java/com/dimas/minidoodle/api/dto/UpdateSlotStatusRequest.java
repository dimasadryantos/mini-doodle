package com.dimas.minidoodle.api.dto;

import com.dimas.minidoodle.domain.SlotStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSlotStatusRequest(@NotNull SlotStatus status) {
}