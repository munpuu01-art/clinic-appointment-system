package com.clinic.dto;

import java.time.LocalTime;

/** ช่องเวลา 1 ช่องพร้อมสถานะว่าง/เต็ม สำหรับหน้าจอจองนัด */
public record SlotResponse(
        LocalTime startTime,
        LocalTime endTime,
        boolean available,
        String unavailableReason,
        Long scheduleId
) { }
