package com.clinic.controller;

import com.clinic.dto.*;
import com.clinic.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) { this.doctorService = doctorService; }

    @GetMapping
    public List<DoctorResponse> findAll(@RequestParam(required = false) Long specialtyId) {
        return doctorService.findAll(specialtyId);
    }

    @GetMapping("/{id}")
    public DoctorResponse findById(@PathVariable Long id) { return doctorService.findById(id); }

    @PostMapping
    public ResponseEntity<DoctorResponse> create(@Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.create(request));
    }

    @PostMapping("/{id}/schedules")
    public ResponseEntity<ScheduleResponse> addSchedule(@PathVariable Long id,
                                                        @Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.addSchedule(id, request));
    }

    @DeleteMapping("/{id}/schedules/{scheduleId}")
    public ResponseEntity<Void> removeSchedule(@PathVariable Long id, @PathVariable Long scheduleId) {
        doctorService.removeSchedule(id, scheduleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/leaves")
    public ResponseEntity<Void> addLeave(@PathVariable Long id, @RequestBody Map<String, String> body) {
        doctorService.addLeave(id, LocalDate.parse(body.get("date")), body.get("reason"));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** ช่องเวลาทั้งวัน พร้อมเหตุผลกรณีจองไม่ได้ */
    @GetMapping("/{id}/slots")
    public List<SlotResponse> slots(@PathVariable Long id,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                    @RequestParam(required = false) Long patientId) {
        return doctorService.availableSlots(id, date, patientId);
    }
}
