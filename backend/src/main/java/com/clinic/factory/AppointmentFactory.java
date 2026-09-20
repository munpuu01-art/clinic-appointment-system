package com.clinic.factory;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentStatus;
import com.clinic.domain.appointment.AppointmentType;
import com.clinic.domain.common.TimeSlot;
import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.doctor.DoctorSchedule;
import com.clinic.domain.person.Patient;
import com.clinic.domain.pricing.FeeCalculator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Factory Method Pattern:
 * รวมตรรกะการ "ประกอบ" นัดหมายแต่ละประเภทไว้ที่เดียว
 * (walk-in ยืนยันอัตโนมัติ, นัดล่วงหน้าเริ่มที่ REQUESTED)
 */
@Component
public class AppointmentFactory {

    private final DocumentNumberGenerator numberGenerator;
    private final FeeCalculator feeCalculator;

    public AppointmentFactory(DocumentNumberGenerator numberGenerator, FeeCalculator feeCalculator) {
        this.numberGenerator = numberGenerator;
        this.feeCalculator = feeCalculator;
    }

    /** นัดหมายล่วงหน้าตามตารางแพทย์ */
    public Appointment createScheduled(Patient patient, Doctor doctor, DoctorSchedule schedule,
                                       LocalDate date, TimeSlot slot, AppointmentType type,
                                       String symptomNote, String createdBy) {
        Appointment appointment = Appointment.builder()
                .appointmentNo(numberGenerator.nextAppointmentNo(date))
                .patient(patient)
                .doctor(doctor)
                .schedule(schedule)
                .date(date)
                .slot(slot)
                .type(type)
                .status(AppointmentStatus.REQUESTED)
                .symptomNote(symptomNote)
                .createdBy(createdBy)
                .build();
        appointment.applyFee(feeCalculator.calculate(appointment));
        return appointment;
    }

    /** Walk-in: ไม่ต้องมีตารางล่วงหน้า และยืนยันให้ทันทีเพื่อเข้าคิวได้เลย */
    public Appointment createWalkIn(Patient patient, Doctor doctor, String symptomNote, String createdBy) {
        LocalDate today = LocalDate.now();
        LocalTime start = LocalTime.now().withSecond(0).withNano(0);
        int minutes = doctor.getSpecialty().getDefaultSlotMinutes();

        Appointment appointment = Appointment.builder()
                .appointmentNo(numberGenerator.nextAppointmentNo(today))
                .patient(patient)
                .doctor(doctor)
                .date(today)
                .time(start, minutes)
                .type(AppointmentType.WALK_IN)
                .status(AppointmentStatus.CONFIRMED)
                .symptomNote(symptomNote)
                .createdBy(createdBy)
                .build();
        appointment.applyFee(feeCalculator.calculate(appointment));
        return appointment;
    }
}
