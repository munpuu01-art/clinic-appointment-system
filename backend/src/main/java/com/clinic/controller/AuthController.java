package com.clinic.controller;

import com.clinic.dto.*;
import com.clinic.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** จุดเข้าใช้งานระบบยืนยันตัวตน (บางเส้นทางเปิดสาธารณะ) */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** เข้าสู่ระบบ — ใช้ได้ทั้ง admin เจ้าหน้าที่ แพทย์ และผู้ป่วย */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** ผู้ป่วยสมัครใช้งานพอร์ทัลด้วยตนเอง แล้วเข้าสู่ระบบทันที */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody PatientRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerPatient(request));
    }

    /** ข้อมูลผู้ใช้ที่ล็อกอินอยู่ — ใช้ตอนหน้าเว็บโหลดใหม่เพื่อตรวจว่า token ยังใช้ได้ */
    @GetMapping("/me")
    public AccountResponse me() {
        return authService.currentUser();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
