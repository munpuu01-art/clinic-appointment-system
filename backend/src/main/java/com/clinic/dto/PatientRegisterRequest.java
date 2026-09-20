package com.clinic.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** ผู้ป่วยสมัครใช้งานพอร์ทัลด้วยตนเอง: สร้างทั้งเวชระเบียนและบัญชีผู้ใช้ */
public record PatientRegisterRequest(
        @NotBlank(message = "กรุณากรอกชื่อผู้ใช้")
        @Size(min = 4, message = "ชื่อผู้ใช้ต้องยาวอย่างน้อย 4 ตัวอักษร") String username,
        @NotBlank(message = "กรุณากรอกรหัสผ่าน")
        @Size(min = 8, message = "รหัสผ่านต้องยาวอย่างน้อย 8 ตัวอักษร") String password,
        @NotNull(message = "กรุณากรอกข้อมูลผู้ป่วย") @Valid PatientRequest patient
) { }
