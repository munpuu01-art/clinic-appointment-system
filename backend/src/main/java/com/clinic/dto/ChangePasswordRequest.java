package com.clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "กรุณากรอกรหัสผ่านเดิม") String currentPassword,
        @NotBlank(message = "กรุณากรอกรหัสผ่านใหม่")
        @Size(min = 8, message = "รหัสผ่านใหม่ต้องยาวอย่างน้อย 8 ตัวอักษร") String newPassword
) { }
