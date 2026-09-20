package com.clinic.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "กรุณากรอกชื่อผู้ใช้") String username,
        @NotBlank(message = "กรุณากรอกรหัสผ่าน") String password
) { }
