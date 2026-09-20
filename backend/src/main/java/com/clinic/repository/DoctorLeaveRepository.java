package com.clinic.repository;

import com.clinic.domain.doctor.DoctorLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DoctorLeaveRepository extends JpaRepository<DoctorLeave, Long> {
    List<DoctorLeave> findByDoctorIdAndLeaveDate(Long doctorId, LocalDate date);
    List<DoctorLeave> findByDoctorIdAndLeaveDateBetween(Long doctorId, LocalDate from, LocalDate to);
}
