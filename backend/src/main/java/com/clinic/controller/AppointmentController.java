package com.clinic.controller;

import com.clinic.dto.*;
import com.clinic.service.AppointmentService;
import com.clinic.service.MedicalRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    public AppointmentController(AppointmentService appointmentService,
                                 MedicalRecordService medicalRecordService) {
        this.appointmentService = appointmentService;
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping
    public Page<AppointmentResponse> byDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return appointmentService.findByDate(date, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public AppointmentResponse findById(@PathVariable Long id) { return appointmentService.findById(id); }

    @PostMapping
    public ResponseEntity<AppointmentResponse> book(@Valid @RequestBody BookAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(request));
    }

    @PatchMapping("/{id}/confirm")
    public AppointmentResponse confirm(@PathVariable Long id) { return appointmentService.confirm(id); }

    @PatchMapping("/{id}/reschedule")
    public AppointmentResponse reschedule(@PathVariable Long id, @Valid @RequestBody RescheduleRequest request) {
        return appointmentService.reschedule(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id, @Valid @RequestBody CancelRequest request) {
        return appointmentService.cancel(id, request);
    }

    @PatchMapping("/{id}/check-in")
    public AppointmentResponse checkIn(@PathVariable Long id) { return appointmentService.checkIn(id); }

    @PatchMapping("/{id}/start")
    public AppointmentResponse start(@PathVariable Long id) { return appointmentService.startExam(id); }

    @PatchMapping("/{id}/complete")
    public AppointmentResponse complete(@PathVariable Long id) { return appointmentService.complete(id); }

    @PatchMapping("/{id}/no-show")
    public AppointmentResponse noShow(@PathVariable Long id) { return appointmentService.markNoShow(id); }

    @GetMapping("/{id}/medical-record")
    public MedicalRecordResponse medicalRecord(@PathVariable Long id) {
        return medicalRecordService.findByAppointment(id);
    }

    @PutMapping("/{id}/medical-record")
    public MedicalRecordResponse saveMedicalRecord(@PathVariable Long id,
                                                   @RequestBody MedicalRecordRequest request) {
        return medicalRecordService.saveForAppointment(id, request);
    }
}
