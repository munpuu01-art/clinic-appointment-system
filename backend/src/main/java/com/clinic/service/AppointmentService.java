package com.clinic.service;

import com.clinic.domain.appointment.Appointment;
import com.clinic.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    AppointmentResponse book(BookAppointmentRequest request);
    AppointmentResponse confirm(Long id);
    AppointmentResponse reschedule(Long id, RescheduleRequest request);
    AppointmentResponse cancel(Long id, CancelRequest request);
    AppointmentResponse checkIn(Long id);
    AppointmentResponse startExam(Long id);
    AppointmentResponse complete(Long id);
    AppointmentResponse markNoShow(Long id);
    AppointmentResponse findById(Long id);
    Page<AppointmentResponse> findByDate(LocalDate date, Pageable pageable);
    List<AppointmentResponse> findByPatient(Long patientId);
    Appointment getEntity(Long id);
}
