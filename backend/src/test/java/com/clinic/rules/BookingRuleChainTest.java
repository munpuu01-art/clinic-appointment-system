package com.clinic.rules;

import com.clinic.config.ClinicProperties;
import com.clinic.domain.appointment.AppointmentType;
import com.clinic.domain.common.ContactInfo;
import com.clinic.domain.common.Gender;
import com.clinic.domain.common.TimeSlot;
import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.doctor.DoctorSchedule;
import com.clinic.domain.doctor.Specialty;
import com.clinic.domain.person.Patient;
import com.clinic.exception.BusinessRuleException;
import com.clinic.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** TC-11..TC-14 : ทดสอบโซ่กฎการจอง */
class BookingRuleChainTest {

    private BookingRuleChain chain;
    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        ClinicProperties props = new ClinicProperties();
        AppointmentRepository repo = mock(AppointmentRepository.class);
        when(repo.findActiveByDoctorAndDate(any(), any())).thenReturn(List.of());
        when(repo.findActiveByPatientAndDate(any(), any())).thenReturn(List.of());

        chain = new BookingRuleChain(List.of(
                new PastDateRule(props),
                new AdvanceWindowRule(props),
                new DoctorAvailabilityRule(),
                new SlotAlignmentRule(),
                new DoctorDoubleBookingRule(repo),
                new PatientOverlapRule(repo),
                new PatientDailyLimitRule(repo, props)));

        Specialty specialty = new Specialty("INT", "อายุรกรรม", 20, BigDecimal.valueOf(500));
        doctor = new Doctor("ว.1", specialty, "หมอ", "ทดสอบ", Gender.MALE,
                LocalDate.of(1980, 1, 1), "1", new ContactInfo("0812345678", null, null));
        LocalDate target = nextMonday();
        DoctorSchedule schedule = new DoctorSchedule(target.getDayOfWeek(),
                LocalTime.of(9, 0), LocalTime.of(12, 0), 20, "A1");
        schedule.setEffectiveFrom(LocalDate.now().minusMonths(1));
        doctor.addSchedule(schedule);

        patient = new Patient("HN-0001", "คนไข้", "ทดสอบ", Gender.FEMALE,
                LocalDate.of(1990, 1, 1), "2", new ContactInfo("0898765432", null, null));
    }

    private LocalDate nextMonday() {
        LocalDate d = LocalDate.now().plusDays(1);
        while (d.getDayOfWeek() != java.time.DayOfWeek.MONDAY) d = d.plusDays(1);
        return d;
    }

    @Test
    @DisplayName("TC-11 ช่องเวลาที่ถูกต้องต้องผ่านทุกกฎ")
    void validBookingPasses() {
        BookingContext ctx = BookingContext.of(patient, doctor, nextMonday(),
                TimeSlot.of(LocalTime.of(9, 0), 20), AppointmentType.NEW_CASE);
        assertDoesNotThrow(() -> chain.validate(ctx));
    }

    @Test
    @DisplayName("TC-12 จองย้อนหลังต้องถูกปฏิเสธ")
    void pastDateRejected() {
        BookingContext ctx = BookingContext.of(patient, doctor, LocalDate.now().minusDays(1),
                TimeSlot.of(LocalTime.of(9, 0), 20), AppointmentType.NEW_CASE);
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> chain.validate(ctx));
        assertEquals("PAST_DATE", ex.getCode());
    }

    @Test
    @DisplayName("TC-13 จองนอกเวลาออกตรวจต้องถูกปฏิเสธ")
    void outOfScheduleRejected() {
        BookingContext ctx = BookingContext.of(patient, doctor, nextMonday(),
                TimeSlot.of(LocalTime.of(14, 0), 20), AppointmentType.NEW_CASE);
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> chain.validate(ctx));
        assertEquals("OUT_OF_SCHEDULE", ex.getCode());
    }

    @Test
    @DisplayName("TC-14 เวลาที่ไม่ตรงช่อง (09:10) ต้องถูกปฏิเสธ")
    void unalignedSlotRejected() {
        BookingContext ctx = BookingContext.of(patient, doctor, nextMonday(),
                TimeSlot.of(LocalTime.of(9, 10), 20), AppointmentType.NEW_CASE);
        assertThrows(BusinessRuleException.class, () -> chain.validate(ctx));
    }
}
