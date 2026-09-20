package com.clinic.dto;

import com.clinic.domain.queue.QueuePriority;
import jakarta.validation.constraints.NotNull;

public record WalkInRequest(
        @NotNull Long patientId,
        @NotNull Long doctorId,
        QueuePriority priority,
        String symptomNote,
        String createdBy
) { }
