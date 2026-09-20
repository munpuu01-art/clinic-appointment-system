package com.clinic.repository;

import com.clinic.domain.queue.QueueStatus;
import com.clinic.domain.queue.QueueTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QueueTicketRepository extends JpaRepository<QueueTicket, Long> {

    List<QueueTicket> findByDoctorIdAndQueueDate(Long doctorId, LocalDate date);

    List<QueueTicket> findByQueueDateAndStatus(LocalDate date, QueueStatus status);

    Optional<QueueTicket> findByAppointmentId(Long appointmentId);

    boolean existsByPatientIdAndQueueDateAndStatusIn(Long patientId, LocalDate date, List<QueueStatus> statuses);

    @Query("SELECT COALESCE(MAX(q.sequenceNo), 0) FROM QueueTicket q WHERE q.doctor.id = :doctorId AND q.queueDate = :date")
    int maxSequenceNo(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(q) FROM QueueTicket q WHERE q.queueDate = :date")
    long countByDate(@Param("date") LocalDate date);
}
