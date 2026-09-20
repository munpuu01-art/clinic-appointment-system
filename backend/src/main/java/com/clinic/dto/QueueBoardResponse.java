package com.clinic.dto;

import java.time.LocalDate;
import java.util.List;

/** ข้อมูลทั้งหมดของจอแสดงคิว 1 ห้องตรวจ */
public record QueueBoardResponse(
        Long doctorId,
        String doctorName,
        String roomNo,
        LocalDate date,
        String strategy,
        QueueTicketResponse nowServing,
        List<QueueTicketResponse> waiting,
        int waitingCount,
        int doneCount,
        int estimatedWaitMinutes
) { }
