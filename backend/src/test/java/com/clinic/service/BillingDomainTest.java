package com.clinic.service;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentType;
import com.clinic.domain.billing.*;
import com.clinic.domain.common.ContactInfo;
import com.clinic.domain.common.Gender;
import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.doctor.Specialty;
import com.clinic.domain.person.Patient;
import com.clinic.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/** TC-15..TC-17 : ทดสอบการคิดเงินและการชำระเงิน */
class BillingDomainTest {

    private Invoice newInvoice() {
        Specialty specialty = new Specialty("INT", "อายุรกรรม", 20, BigDecimal.valueOf(500));
        Doctor doctor = new Doctor("ว.1", specialty, "หมอ", "ทดสอบ", Gender.MALE,
                LocalDate.of(1980, 1, 1), "1", new ContactInfo("0812345678", null, null));
        Patient patient = new Patient("HN-0001", "คนไข้", "ทดสอบ", Gender.FEMALE,
                LocalDate.of(1990, 1, 1), "2", new ContactInfo("0898765432", null, null));
        Appointment appointment = Appointment.builder()
                .appointmentNo("AP-TEST-001").patient(patient).doctor(doctor)
                .date(LocalDate.now()).time(LocalTime.of(9, 0), 20)
                .type(AppointmentType.NEW_CASE).build();
        return new Invoice("INV-TEST-0001", appointment);
    }

    @Test
    @DisplayName("TC-15 ยอดรวมต้องเท่ากับผลรวมรายการหักส่วนลด")
    void calculateTotal() {
        Invoice inv = newInvoice();
        inv.addItem(new InvoiceItem("ค่าตรวจ", 1, BigDecimal.valueOf(500)));
        inv.addItem(new InvoiceItem("ค่ายา", 2, BigDecimal.valueOf(120)));
        inv.applyDiscount(BigDecimal.valueOf(40));

        assertEquals(0, BigDecimal.valueOf(700).compareTo(inv.calculateTotal()));
    }

    @Test
    @DisplayName("TC-16 ออกใบแจ้งหนี้เปล่าไม่ได้")
    void cannotIssueEmptyInvoice() {
        assertThrows(BusinessRuleException.class, () -> newInvoice().issue());
    }

    @Test
    @DisplayName("TC-17 ชำระบางส่วนแล้วยอดคงค้างต้องลดลง และชำระครบจึงเป็น PAID")
    void partialThenFullPayment() {
        Invoice inv = newInvoice();
        inv.addItem(new InvoiceItem("ค่าตรวจ", 1, BigDecimal.valueOf(500)));
        inv.issue();

        inv.pay(new Payment(BigDecimal.valueOf(200), PaymentMethod.CASH, null));
        assertEquals(InvoiceStatus.PARTIALLY_PAID, inv.getStatus());
        assertEquals(0, BigDecimal.valueOf(300).compareTo(inv.outstandingAmount()));

        inv.pay(new Payment(BigDecimal.valueOf(300), PaymentMethod.PROMPTPAY, "PP-001"));
        assertEquals(InvoiceStatus.PAID, inv.getStatus());
        assertTrue(inv.isSettled());
    }
}
