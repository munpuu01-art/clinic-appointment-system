package com.clinic.domain.appointment;

import com.clinic.domain.appointment.state.AppointmentState;
import com.clinic.domain.appointment.state.AppointmentStateFactory;
import com.clinic.domain.common.BaseEntity;
import com.clinic.domain.common.TimeSlot;
import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.doctor.DoctorSchedule;
import com.clinic.domain.medical.MedicalRecord;
import com.clinic.domain.person.Patient;
import com.clinic.domain.queue.QueueTicket;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Aggregate Root ของการนัดหมาย
 * ผูก Patient + Doctor + Schedule + ช่วงเวลา และคุม workflow ผ่าน State Pattern
 */
@Entity
@Table(name = "appointment",
       uniqueConstraints = @UniqueConstraint(name = "uk_appointment_no", columnNames = "appointment_no"),
       indexes = {
           @Index(name = "idx_appt_doctor_date", columnList = "doctor_id, appointment_date"),
           @Index(name = "idx_appt_patient_date", columnList = "patient_id, appointment_date")
       })
public class Appointment extends BaseEntity {

    @Column(name = "appointment_no", nullable = false, length = 20)
    private String appointmentNo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private DoctorSchedule schedule;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AppointmentType type = AppointmentType.NEW_CASE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.REQUESTED;

    @Column(name = "symptom_note", length = 500)
    private String symptomNote;

    @Column(name = "cancel_reason", length = 300)
    private String cancelReason;

    @Column(name = "fee", precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_by", length = 60)
    private String createdBy;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private QueueTicket queueTicket;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MedicalRecord medicalRecord;

    protected Appointment() { }

    private Appointment(Builder b) {
        this.appointmentNo = b.appointmentNo;
        this.patient = b.patient;
        this.doctor = b.doctor;
        this.schedule = b.schedule;
        this.appointmentDate = b.date;
        this.startTime = b.start;
        this.endTime = b.end;
        this.type = b.type;
        this.symptomNote = b.symptomNote;
        this.createdBy = b.createdBy;
        this.status = b.status;
        this.fee = b.fee;
    }

    // ---------- State Pattern: พฤติกรรมขึ้นกับสถานะปัจจุบัน ----------

    @Transient
    public AppointmentState state() {
        return AppointmentStateFactory.of(status);
    }

    public void confirm()                { state().confirm(this); }
    public void checkIn()                { state().checkIn(this); }
    public void start()                  { state().start(this); }
    public void complete()               { state().complete(this); }
    public void cancel(String reason)    { state().cancel(this, reason); }
    public void markNoShow()             { state().noShow(this); }

    /** เรียกจากคลาส State เท่านั้น */
    public void applyStatus(AppointmentStatus newStatus) {
        this.status = newStatus;
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case CHECKED_IN -> this.checkedInAt = now;
            case IN_PROGRESS -> this.startedAt = now;
            case COMPLETED -> this.completedAt = now;
            default -> { }
        }
    }

    public void applyCancelReason(String reason) { this.cancelReason = reason; }

    // ---------- Business logic ----------

    public TimeSlot timeSlot() { return new TimeSlot(startTime, endTime); }

    public LocalDateTime startDateTime() { return LocalDateTime.of(appointmentDate, startTime); }

    public boolean isPast() { return startDateTime().isBefore(LocalDateTime.now()); }

    public boolean isToday() { return appointmentDate.equals(LocalDate.now()); }

    /** ชนเวลากับนัดอื่นหรือไม่ (ใช้ตรวจ double booking) */
    public boolean conflictsWith(Appointment other) {
        return status.isActive()
                && other.getStatus().isActive()
                && appointmentDate.equals(other.appointmentDate)
                && timeSlot().overlaps(other.timeSlot());
    }

    /** เลื่อนนัด — อนุญาตเฉพาะก่อนเช็คอิน */
    public void reschedule(LocalDate newDate, LocalTime newStart, DoctorSchedule newSchedule) {
        if (status != AppointmentStatus.REQUESTED && status != AppointmentStatus.CONFIRMED) {
            throw new com.clinic.exception.InvalidAppointmentStateException(status, "เลื่อนนัด");
        }
        int minutes = timeSlot().durationMinutes();
        this.appointmentDate = newDate;
        this.startTime = newStart;
        this.endTime = newStart.plusMinutes(minutes);
        this.schedule = newSchedule;
    }

    public int waitedMinutes() {
        if (checkedInAt == null) return 0;
        LocalDateTime until = startedAt != null ? startedAt : LocalDateTime.now();
        return (int) java.time.Duration.between(checkedInAt, until).toMinutes();
    }

    public void attachQueueTicket(QueueTicket ticket) { this.queueTicket = ticket; }
    public void attachMedicalRecord(MedicalRecord record) { this.medicalRecord = record; }
    public void applyFee(BigDecimal fee) { this.fee = fee; }

    // ---------- Builder Pattern ----------

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String appointmentNo;
        private Patient patient;
        private Doctor doctor;
        private DoctorSchedule schedule;
        private LocalDate date;
        private LocalTime start;
        private LocalTime end;
        private AppointmentType type = AppointmentType.NEW_CASE;
        private AppointmentStatus status = AppointmentStatus.REQUESTED;
        private String symptomNote;
        private String createdBy;
        private BigDecimal fee;

        public Builder appointmentNo(String v) { this.appointmentNo = v; return this; }
        public Builder patient(Patient v) { this.patient = v; return this; }
        public Builder doctor(Doctor v) { this.doctor = v; return this; }
        public Builder schedule(DoctorSchedule v) { this.schedule = v; return this; }
        public Builder date(LocalDate v) { this.date = v; return this; }
        public Builder slot(TimeSlot v) { this.start = v.getStart(); this.end = v.getEnd(); return this; }
        public Builder time(LocalTime start, int minutes) {
            this.start = start; this.end = start.plusMinutes(minutes); return this;
        }
        public Builder type(AppointmentType v) { this.type = v; return this; }
        public Builder status(AppointmentStatus v) { this.status = v; return this; }
        public Builder symptomNote(String v) { this.symptomNote = v; return this; }
        public Builder createdBy(String v) { this.createdBy = v; return this; }
        public Builder fee(BigDecimal v) { this.fee = v; return this; }

        public Appointment build() {
            if (patient == null || doctor == null) throw new IllegalStateException("ต้องระบุผู้ป่วยและแพทย์");
            if (date == null || start == null || end == null) throw new IllegalStateException("ต้องระบุวันและเวลา");
            return new Appointment(this);
        }
    }

    // ---------- Getters ----------

    public String getAppointmentNo() { return appointmentNo; }
    public void setAppointmentNo(String v) { this.appointmentNo = v; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public DoctorSchedule getSchedule() { return schedule; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public AppointmentType getType() { return type; }
    public AppointmentStatus getStatus() { return status; }
    public String getSymptomNote() { return symptomNote; }
    public void setSymptomNote(String v) { this.symptomNote = v; }
    public String getCancelReason() { return cancelReason; }
    public BigDecimal getFee() { return fee; }
    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getCreatedBy() { return createdBy; }
    public QueueTicket getQueueTicket() { return queueTicket; }
    public MedicalRecord getMedicalRecord() { return medicalRecord; }
}
