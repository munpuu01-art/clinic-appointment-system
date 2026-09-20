package com.clinic.domain;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentStatus;
import com.clinic.domain.appointment.AppointmentType;
import com.clinic.domain.common.ContactInfo;
import com.clinic.domain.common.Gender;
import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.doctor.Specialty;
import com.clinic.domain.person.Patient;
import com.clinic.exception.InvalidAppointmentStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/** TC-01..TC-05 : ทดสอบ State Pattern ของนัดหมาย */
class AppointmentStateTest {

    private Appointment newAppointment(LocalDate date) {
        Specialty specialty = new Specialty("INT", "อายุรกรรม", 20, BigDecimal.valueOf(500));
        Doctor doctor = new Doctor("ว.1", specialty, "หมอ", "ทดสอบ", Gender.MALE,
                LocalDate.of(1980, 1, 1), "1", new ContactInfo("0812345678", "d@test.com", null));
        Patient patient = new Patient("HN-0001", "คนไข้", "ทดสอบ", Gender.FEMALE,
                LocalDate.of(1990, 1, 1), "2", new ContactInfo("0898765432", "p@test.com", null));

        return Appointment.builder()
                .appointmentNo("AP-TEST-001")
                .patient(patient).doctor(doctor)
                .date(date).time(LocalTime.of(9, 0), 20)
                .type(AppointmentType.NEW_CASE)
                .build();
    }

    @Test
    @DisplayName("TC-01 นัดใหม่ต้องอยู่สถานะรอยืนยัน และยืนยันได้")
    void confirmFromRequested() {
        Appointment a = newAppointment(LocalDate.now().plusDays(1));
        assertEquals(AppointmentStatus.REQUESTED, a.getStatus());
        a.confirm();
        assertEquals(AppointmentStatus.CONFIRMED, a.getStatus());
    }

    @Test
    @DisplayName("TC-02 เช็คอินก่อนยืนยันไม่ได้")
    void cannotCheckInBeforeConfirm() {
        Appointment a = newAppointment(LocalDate.now());
        assertThrows(InvalidAppointmentStateException.class, a::checkIn);
    }

    @Test
    @DisplayName("TC-03 เช็คอินได้เฉพาะวันที่นัดหมาย")
    void checkInOnlyOnAppointmentDate() {
        Appointment tomorrow = newAppointment(LocalDate.now().plusDays(1));
        tomorrow.confirm();
        assertThrows(RuntimeException.class, tomorrow::checkIn);

        Appointment today = newAppointment(LocalDate.now());
        today.confirm();
        today.checkIn();
        assertEquals(AppointmentStatus.CHECKED_IN, today.getStatus());
    }

    @Test
    @DisplayName("TC-04 workflow เต็มรูปแบบ: ยืนยัน -> เช็คอิน -> ตรวจ -> เสร็จ")
    void fullHappyPath() {
        Appointment a = newAppointment(LocalDate.now());
        a.confirm();
        a.checkIn();
        a.start();
        a.complete();
        assertEquals(AppointmentStatus.COMPLETED, a.getStatus());
        assertTrue(a.getStatus().isFinal());
        assertNotNull(a.getCompletedAt());
    }

    @Test
    @DisplayName("TC-05 นัดที่ตรวจเสร็จแล้วยกเลิกไม่ได้")
    void cannotCancelCompleted() {
        Appointment a = newAppointment(LocalDate.now());
        a.confirm();
        a.checkIn();
        a.start();
        a.complete();
        assertThrows(InvalidAppointmentStateException.class, () -> a.cancel("ขอยกเลิก"));
    }
}
