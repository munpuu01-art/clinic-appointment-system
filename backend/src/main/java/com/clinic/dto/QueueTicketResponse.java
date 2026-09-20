package com.clinic.dto;

import com.clinic.domain.queue.QueuePriority;
import com.clinic.domain.queue.QueueStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record QueueTicketResponse(
        Long id,
        String ticketNo,
        LocalDate queueDate,
        Long doctorId,
        String doctorName,
        Long patientId,
        String patientName,
        String hn,
        QueuePriority priority,
        String priorityLabel,
        QueueStatus status,
        String statusLabel,
        int sequenceNo,
        LocalDateTime issuedAt,
        LocalDateTime calledAt,
        String counterNo,
        int waitingMinutes,
        Long appointmentId
) { }
