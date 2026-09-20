package com.clinic.factory;

import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.repository.QueueTicketRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ผู้รับผิดชอบเดียวในการออกเลขเอกสารทุกชนิด (HN / เลขนัด / เลขคิว / เลขใบเสร็จ)
 * แยกออกมาเพื่อไม่ให้ Service ต้องรู้รูปแบบเลขเอกสาร
 */
@Component
public class DocumentNumberGenerator {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final QueueTicketRepository queueTicketRepository;

    public DocumentNumberGenerator(PatientRepository patientRepository,
                                   AppointmentRepository appointmentRepository,
                                   QueueTicketRepository queueTicketRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.queueTicketRepository = queueTicketRepository;
    }

    /** HN-2026-0001 */
    public String nextHn() {
        String year = String.valueOf(LocalDate.now().getYear());
        long running = patientRepository.countByHnYear(year) + 1;
        return String.format("HN-%s-%04d", year, running);
    }

    /** AP-20260919-005 */
    public String nextAppointmentNo(LocalDate date) {
        long running = appointmentRepository.countByAppointmentDate(date) + 1;
        return String.format("AP-%s-%03d", date.format(YMD), running);
    }

    /** เลขคิวบนจอ เช่น A012 (A = อักษรประจำแผนก) */
    public String nextTicketNo(LocalDate date, String specialtyCode, int sequenceNo) {
        char prefix = specialtyCode == null || specialtyCode.isEmpty()
                ? 'Q' : Character.toUpperCase(specialtyCode.charAt(0));
        return String.format("%c%03d", prefix, sequenceNo);
    }

    /** INV-20260919-0007 */
    public String nextInvoiceNo(LocalDate date) {
        long running = queueTicketRepository.countByDate(date) + 1;
        return String.format("INV-%s-%04d", date.format(YMD), running);
    }
}
