package com.clinic.controller;

import com.clinic.domain.appointment.AppointmentType;
import com.clinic.domain.queue.QueuePriority;
import com.clinic.dto.DashboardResponse;
import com.clinic.dto.SpecialtyResponse;
import com.clinic.service.DashboardService;
import com.clinic.service.DoctorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** ข้อมูลอ้างอิงและภาพรวมสำหรับหน้า Dashboard */
@RestController
@RequestMapping("/api")
public class LookupController {

    private final DoctorService doctorService;
    private final DashboardService dashboardService;

    public LookupController(DoctorService doctorService, DashboardService dashboardService) {
        this.doctorService = doctorService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/specialties")
    public List<SpecialtyResponse> specialties() { return doctorService.specialties(); }

    @GetMapping("/appointment-types")
    public List<Map<String, Object>> appointmentTypes() {
        return Arrays.stream(AppointmentType.values())
                .map(t -> Map.<String, Object>of("value", t.name(), "label", t.getLabel()))
                .toList();
    }

    @GetMapping("/queue-priorities")
    public List<Map<String, Object>> queuePriorities() {
        return Arrays.stream(QueuePriority.values())
                .map(p -> Map.<String, Object>of("value", p.name(), "label", p.getLabel()))
                .toList();
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@RequestParam(required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dashboardService.summary(date);
    }
}
