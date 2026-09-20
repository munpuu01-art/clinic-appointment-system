package com.clinic.controller;

import com.clinic.dto.PatientRequest;
import com.clinic.dto.PatientResponse;
import com.clinic.service.AppointmentService;
import com.clinic.service.MedicalRecordService;
import com.clinic.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    public PatientController(PatientService patientService,
                             AppointmentService appointmentService,
                             MedicalRecordService medicalRecordService) {
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping
    public Page<PatientResponse> search(@RequestParam(defaultValue = "") String q,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return patientService.search(q, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public PatientResponse findById(@PathVariable Long id) { return patientService.findById(id); }

    @GetMapping("/hn/{hn}")
    public PatientResponse findByHn(@PathVariable String hn) { return patientService.findByHn(hn); }

    @PostMapping
    public ResponseEntity<PatientResponse> register(@Valid @RequestBody PatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.register(request));
    }

    @PutMapping("/{id}")
    public PatientResponse update(@PathVariable Long id, @Valid @RequestBody PatientRequest request) {
        return patientService.update(id, request);
    }

    @GetMapping("/{id}/appointments")
    public Object appointments(@PathVariable Long id) { return appointmentService.findByPatient(id); }

    @GetMapping("/{id}/medical-records")
    public Object medicalRecords(@PathVariable Long id) { return medicalRecordService.historyOfPatient(id); }
}
