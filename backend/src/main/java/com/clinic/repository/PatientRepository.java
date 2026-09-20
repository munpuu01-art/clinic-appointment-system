package com.clinic.repository;

import com.clinic.domain.person.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Repository Pattern ผ่าน Spring Data JPA */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByHn(String hn);
    Optional<Patient> findByNationalId(String nationalId);
    boolean existsByHn(String hn);

    @Query("""
           SELECT p FROM Patient p
           WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(p.lastName)  LIKE LOWER(CONCAT('%', :q, '%'))
              OR p.hn LIKE CONCAT('%', :q, '%')
              OR p.contact.phone LIKE CONCAT('%', :q, '%')
           """)
    Page<Patient> search(@Param("q") String keyword, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.hn LIKE CONCAT('HN-', :year, '-%')")
    long countByHnYear(@Param("year") String year);
}
