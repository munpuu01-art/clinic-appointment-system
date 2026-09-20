package com.clinic.dto;

import com.clinic.domain.auth.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** ผู้ดูแลระบบสร้างบัญชีให้เจ้าหน้าที่ แพทย์ หรือผู้ป่วยที่มีอยู่แล้ว */
public record CreateAccountRequest(
        @NotBlank(message = "กรุณากรอกชื่อผู้ใช้") String username,
        @NotBlank(message = "กรุณากรอกรหัสผ่าน")
        @Size(min = 8, message = "รหัสผ่านต้องยาวอย่างน้อย 8 ตัวอักษร") String password,
        @NotNull(message = "กรุณาเลือกบทบาท") Role role,
        Long personId,
        String displayName
) { }
