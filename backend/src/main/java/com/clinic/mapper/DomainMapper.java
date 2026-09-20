package com.clinic.mapper;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentStatus;
import com.clinic.domain.billing.Invoice;
import com.clinic.domain.billing.InvoiceItem;
import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.doctor.DoctorSchedule;
import com.clinic.domain.doctor.Specialty;
import com.clinic.domain.medical.MedicalRecord;
import com.clinic.domain.medical.Vitals;
import com.clinic.domain.person.Patient;
import com.clinic.domain.queue.QueueTicket;
import com.clinic.dto.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * แปลง Entity <-> DTO ไว้ที่เดียว
 * ป้องกันไม่ให้ entity (และ lazy proxy) รั่วออกไปที่ชั้น API
 */
@Component
public class DomainMapper {

    public PatientResponse toDto(Patient p) {
        return new PatientResponse(
                p.getId(), p.getHn(), p.getFullName(), p.getGender(), p.getBirthDate(),
                p.getAge(), p.isElderly(),
                p.getContact() == null ? null : p.getContact().getPhone(),
                p.getContact() == null ? null : p.getContact().getEmail(),
                p.getAddress() == null ? null : p.getAddress().fullAddress(),
                p.getBloodType(), p.getAllergies(), p.getChronicDisease(),
                p.getAppointments() == null ? 0 : p.getAppointments().size()
        );
    }

    public SpecialtyResponse toDto(Specialty s) {
        return new SpecialtyResponse(s.getId(), s.getCode(), s.getName(),
                s.getDefaultSlotMinutes(), s.getBaseFee(),
                s.getDoctors() == null ? 0 : s.getDoctors().size());
    }

    public ScheduleResponse toDto(DoctorSchedule s) {
        return new ScheduleResponse(s.getId(), s.getDoctor().getId(), s.getDayOfWeek(),
                s.getStartTime(), s.getEndTime(), s.getSlotMinutes(), s.getCapacityPerSlot(),
                s.getRoomNo(), s.getEffectiveFrom(), s.getEffectiveTo(), s.isActive(),
                s.totalCapacity());
    }

    public DoctorResponse toDto(Doctor d) {
        List<ScheduleResponse> schedules = d.getSchedules().stream().map(this::toDto).toList();
        return new DoctorResponse(d.getId(), d.getLicenseNo(), d.getFullName(), d.getDisplayName(),
                d.getSpecialty().getName(), d.getSpecialty().getId(), d.effectiveFee(),
                d.getRoomNo(), d.isActive(), schedules);
    }

    public AppointmentResponse toDto(Appointment a) {
        List<AppointmentStatus> allowed = Arrays.stream(AppointmentStatus.values())
                .filter(s -> a.state().allows(s))
                .toList();
        return new AppointmentResponse(
                a.getId(), a.getAppointmentNo(),
                a.getPatient().getId(), a.getPatient().getFullName(), a.getPatient().getHn(),
                a.getDoctor().getId(), a.getDoctor().getDisplayName(), a.getDoctor().getSpecialty().getName(),
                a.getAppointmentDate(), a.getStartTime(), a.getEndTime(),
                a.getType(), a.getType().getLabel(),
                a.getStatus(), a.getStatus().getLabel(),
                a.getSymptomNote(), a.getCancelReason(), a.getFee(),
                a.getQueueTicket() == null ? null : a.getQueueTicket().getTicketNo(),
                allowed
        );
    }

    public QueueTicketResponse toDto(QueueTicket t) {
        return new QueueTicketResponse(
                t.getId(), t.getTicketNo(), t.getQueueDate(),
                t.getDoctor().getId(), t.getDoctor().getDisplayName(),
                t.getPatient().getId(), t.getPatient().getFullName(), t.getPatient().getHn(),
                t.getPriority(), t.getPriority().getLabel(),
                t.getStatus(), t.getStatus().getLabel(),
                t.getSequenceNo(), t.getIssuedAt(), t.getCalledAt(), t.getCounterNo(),
                t.waitingMinutes(),
                t.getAppointment() == null ? null : t.getAppointment().getId()
        );
    }

    public MedicalRecordResponse toDto(MedicalRecord r) {
        Vitals v = r.getVitals();
        return new MedicalRecordResponse(
                r.getId(), r.getAppointment().getId(), r.getAppointment().getAppointmentNo(),
                r.getPatient().getFullName(), r.getDoctor().getDisplayName(),
                r.getChiefComplaint(), r.getDiagnosis(), r.getTreatment(), r.getPrescription(),
                v == null ? null : v.getTemperatureC(),
                v == null ? null : v.getSystolic(),
                v == null ? null : v.getDiastolic(),
                v == null ? null : v.bmi(),
                r.getFollowUpDate(), r.getCreatedAt()
        );
    }

    public InvoiceResponse toDto(Invoice inv) {
        List<InvoiceResponse.Line> lines = inv.getItems().stream()
                .map(this::toLine).toList();
        return new InvoiceResponse(
                inv.getId(), inv.getInvoiceNo(), inv.getAppointment().getId(),
                inv.getPatient().getFullName(), inv.getStatus(), inv.getStatus().getLabel(),
                lines, inv.getDiscount(), inv.calculateTotal(), inv.paidAmount(), inv.outstandingAmount()
        );
    }

    private InvoiceResponse.Line toLine(InvoiceItem i) {
        return new InvoiceResponse.Line(i.getDescription(), i.getQuantity(), i.getUnitPrice(), i.lineTotal());
    }
}
