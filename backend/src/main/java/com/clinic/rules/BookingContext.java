package com.clinic.rules;

import com.clinic.domain.appointment.AppointmentType;
import com.clinic.domain.common.TimeSlot;
import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.person.Patient;

import java.time.LocalDate;

/** ข้อมูลทั้งหมดที่กฎการจองต้องใช้ตรวจสอบ (immutable) */
public record BookingContext(
        Patient patient,
        Doctor doctor,
        LocalDate date,
        TimeSlot slot,
        AppointmentType type,
        Long excludeAppointmentId   // ใช้ตอนเลื่อนนัด เพื่อไม่ให้ชนกับตัวเอง
) {
    public static BookingContext of(Patient patient, Doctor doctor, LocalDate date,
                                    TimeSlot slot, AppointmentType type) {
        return new BookingContext(patient, doctor, date, slot, type, null);
    }
}
