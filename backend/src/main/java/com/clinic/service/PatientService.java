package com.clinic.service;

import com.clinic.domain.person.Patient;
import com.clinic.dto.PatientRequest;
import com.clinic.dto.PatientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientService {
    PatientResponse register(PatientRequest request);
    PatientResponse update(Long id, PatientRequest request);
    PatientResponse findById(Long id);
    PatientResponse findByHn(String hn);
    Page<PatientResponse> search(String keyword, Pageable pageable);
    Patient getEntity(Long id);
}
