package com.clinic.controller;

import com.clinic.dto.*;
import com.clinic.security.CurrentUser;
import com.clinic.service.AppointmentService;
import com.clinic.service.DoctorService;
import com.clinic.service.MedicalRecordService;
import com.clinic.service.PatientService;
import com.clinic.domain.appointment.AppointmentType;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * พอร์ทัลผู้ป่วย — ทุก endpoint ทำงานกับ "ผู้ป่วยที่ล็อกอินอยู่" เท่านั้น
 * ไม่รับ patientId จากภายนอก จึงไม่มีทางดูข้อมูลของคนอื่นได้
 */
@RestController
@RequestMapping("/api/portal")
@PreAuthorize("hasRole('PATIENT')")
public class PortalController {

    private final CurrentUser currentUser;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final MedicalRecordService medicalRecordService;

    public PortalController(CurrentUser currentUser, PatientService patientService,
                            AppointmentService appointmentService, DoctorService doctorService,
                            MedicalRecordService medicalRecordService) {
        this.currentUser = currentUser;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.medicalRecordService = medicalRecordService;
    }

    /** ข้อมูลผู้ป่วยของตัวเอง */
    @GetMapping("/profile")
    public PatientResponse profile() {
        return patientService.findById(currentUser.requirePatientId());
    }

    /** นัดหมายทั้งหมดของตัวเอง */
    @GetMapping("/appointments")
    public List<AppointmentResponse> appointments() {
        return appointmentService.findByPatient(currentUser.requirePatientId());
    }

    /** รายชื่อแพทย์และแผนก สำหรับหน้าจองนัดของผู้ป่วย */
    @GetMapping("/doctors")
    public List<DoctorResponse> doctors(@RequestParam(required = false) Long specialtyId) {
        return doctorService.findAll(specialtyId);
    }

    @GetMapping("/specialties")
    public List<SpecialtyResponse> specialties() {
        return doctorService.specialties();
    }

    /** ช่องเวลาว่างของแพทย์ — ตรวจกฎโดยอิงผู้ป่วยที่ล็อกอินอยู่ */
    @GetMapping("/doctors/{doctorId}/slots")
    public List<SlotResponse> slots(@PathVariable Long doctorId,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return doctorService.availableSlots(doctorId, date, currentUser.requirePatientId());
    }

    /** จองนัดด้วยตนเอง */
    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponse> book(@Valid @RequestBody SelfBookingRequest request) {
        Long patientId = currentUser.requirePatientId();
        BookAppointmentRequest full = new BookAppointmentRequest(
                patientId,
                request.doctorId(),
                request.date(),
                request.startTime(),
                request.type() == null ? AppointmentType.NEW_CASE : request.type(),
                request.symptomNote(),
                currentUser.displayName());
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(full));
    }

    /** ยกเลิกนัดของตัวเอง — ตรวจความเป็นเจ้าของก่อนเสมอ */
    @PatchMapping("/appointments/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id, @Valid @RequestBody CancelRequest request) {
        assertOwnAppointment(id);
        return appointmentService.cancel(id, new CancelRequest(request.reason(), currentUser.displayName()));
    }

    /** เลื่อนนัดของตัวเอง */
    @PatchMapping("/appointments/{id}/reschedule")
    public AppointmentResponse reschedule(@PathVariable Long id, @Valid @RequestBody RescheduleRequest request) {
        assertOwnAppointment(id);
        return appointmentService.reschedule(id, request);
    }

    /** ประวัติการรักษาของตัวเอง */
    @GetMapping("/medical-records")
    public List<MedicalRecordResponse> medicalRecords() {
        return medicalRecordService.historyOfPatient(currentUser.requirePatientId());
    }

    private void assertOwnAppointment(Long appointmentId) {
        AppointmentResponse appointment = appointmentService.findById(appointmentId);
        currentUser.assertCanAccessPatient(appointment.patientId());
    }
}
