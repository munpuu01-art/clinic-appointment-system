package com.clinic.dto;

import java.time.LocalDateTime;
import java.util.List;

/** รูปแบบ error ที่ส่งกลับให้ frontend เหมือนกันทุกกรณี */
public record ApiError(
        String code,
        String message,
        int status,
        String path,
        List<String> details,
        LocalDateTime timestamp
) {
    public static ApiError of(String code, String message, int status, String path) {
        return new ApiError(code, message, status, path, List.of(), LocalDateTime.now());
    }
}
