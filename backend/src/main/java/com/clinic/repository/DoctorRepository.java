package com.clinic.repository;

import com.clinic.domain.doctor.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByLicenseNo(String licenseNo);
    List<Doctor> findBySpecialtyIdAndActiveTrue(Long specialtyId);
    List<Doctor> findByActiveTrue();

    @Query("""
           SELECT DISTINCT d FROM Doctor d
           LEFT JOIN FETCH d.schedules s
           WHERE d.active = true AND (:specialtyId IS NULL OR d.specialty.id = :specialtyId)
           """)
    List<Doctor> findAvailableDoctors(@Param("specialtyId") Long specialtyId);
}
