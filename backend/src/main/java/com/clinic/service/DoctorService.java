package com.clinic.service;

import com.clinic.domain.doctor.Doctor;
import com.clinic.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface DoctorService {
    DoctorResponse create(DoctorRequest request);
    DoctorResponse findById(Long id);
    List<DoctorResponse> findAll(Long specialtyId);
    ScheduleResponse addSchedule(Long doctorId, ScheduleRequest request);
    void removeSchedule(Long doctorId, Long scheduleId);
    void addLeave(Long doctorId, LocalDate date, String reason);
    /** ช่องเวลาทั้งหมดของแพทย์ในวันนั้น พร้อมสถานะว่าง/เต็ม */
    List<SlotResponse> availableSlots(Long doctorId, LocalDate date, Long patientId);
    List<SpecialtyResponse> specialties();
    Doctor getEntity(Long id);
}
