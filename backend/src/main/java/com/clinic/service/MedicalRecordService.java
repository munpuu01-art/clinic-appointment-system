package com.clinic.service;

import com.clinic.dto.MedicalRecordRequest;
import com.clinic.dto.MedicalRecordResponse;

import java.util.List;

public interface MedicalRecordService {
    MedicalRecordResponse saveForAppointment(Long appointmentId, MedicalRecordRequest request);
    MedicalRecordResponse findByAppointment(Long appointmentId);
    List<MedicalRecordResponse> historyOfPatient(Long patientId);
}
