package com.clinic.domain.queue;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.common.BaseEntity;
import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.person.Patient;
import com.clinic.exception.BusinessRuleException;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** บัตรคิว 1 ใบ ของผู้ป่วย 1 คน ต่อแพทย์ 1 ท่าน ในวันหนึ่ง */
@Entity
@Table(name = "queue_ticket",
       uniqueConstraints = @UniqueConstraint(name = "uk_ticket_no_date",
               columnNames = {"queue_date", "ticket_no"}),
       indexes = @Index(name = "idx_queue_doctor_date", columnList = "doctor_id, queue_date"))
public class QueueTicket extends BaseEntity {

    /** เลขคิวที่แสดงบนจอ เช่น A012 */
    @Column(name = "ticket_no", nullable = false, length = 10)
    private String ticketNo;

    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /** null ได้ กรณี walk-in ที่ยังไม่ผูกนัดหมาย */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 15)
    private QueuePriority priority = QueuePriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private QueueStatus status = QueueStatus.WAITING;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "done_at")
    private LocalDateTime doneAt;

    @Column(name = "counter_no", length = 10)
    private String counterNo;

    @Column(name = "recall_count", nullable = false)
    private int recallCount = 0;

    protected QueueTicket() { }

    public QueueTicket(String ticketNo, LocalDate queueDate, Doctor doctor, Patient patient,
                       QueuePriority priority, int sequenceNo) {
        this.ticketNo = ticketNo;
        this.queueDate = queueDate;
        this.doctor = doctor;
        this.patient = patient;
        this.priority = priority;
        this.sequenceNo = sequenceNo;
    }

    /** ผูกบัตรคิวกับนัดหมาย และอัปเกรดความสำคัญเป็น APPOINTMENT */
    public void linkTo(Appointment appointment) {
        this.appointment = appointment;
        appointment.attachQueueTicket(this);
        if (priority == QueuePriority.NORMAL) {
            this.priority = QueuePriority.APPOINTMENT;
        }
    }

    public void call(String counterNo) {
        requireOpen("เรียกคิว");
        this.status = QueueStatus.CALLED;
        this.counterNo = counterNo;
        this.calledAt = LocalDateTime.now();
    }

    public void recall() {
        if (status != QueueStatus.CALLED) {
            throw new BusinessRuleException("QUEUE_RECALL", "เรียกซ้ำได้เฉพาะคิวที่เรียกไปแล้ว");
        }
        this.recallCount++;
        this.calledAt = LocalDateTime.now();
    }

    public void serve() {
        if (status != QueueStatus.CALLED) {
            throw new BusinessRuleException("QUEUE_SERVE", "ต้องเรียกคิวก่อนจึงเริ่มรับบริการได้");
        }
        this.status = QueueStatus.SERVING;
    }

    public void complete() {
        requireOpen("ปิดคิว");
        this.status = QueueStatus.DONE;
        this.doneAt = LocalDateTime.now();
    }

    public void skip() {
        requireOpen("ข้ามคิว");
        this.status = QueueStatus.SKIPPED;
    }

    /** คิวที่ถูกข้ามสามารถกลับเข้าคิวได้ใหม่ แต่ต่อท้าย */
    public void requeue(int newSequenceNo) {
        if (status != QueueStatus.SKIPPED) {
            throw new BusinessRuleException("QUEUE_REQUEUE", "กลับเข้าคิวได้เฉพาะคิวที่ถูกข้าม");
        }
        this.status = QueueStatus.WAITING;
        this.sequenceNo = newSequenceNo;
        this.calledAt = null;
    }

    private void requireOpen(String action) {
        if (!status.isOpen()) {
            throw new BusinessRuleException("QUEUE_CLOSED", "ไม่สามารถ" + action + " สถานะปัจจุบันคือ " + status.getLabel());
        }
    }

    public int waitingMinutes() {
        LocalDateTime until = calledAt != null ? calledAt : LocalDateTime.now();
        return (int) Duration.between(issuedAt, until).toMinutes();
    }

    public boolean isWaiting() { return status == QueueStatus.WAITING; }

    public String getTicketNo() { return ticketNo; }
    public LocalDate getQueueDate() { return queueDate; }
    public Doctor getDoctor() { return doctor; }
    public Patient getPatient() { return patient; }
    public Appointment getAppointment() { return appointment; }
    public QueuePriority getPriority() { return priority; }
    public void setPriority(QueuePriority v) { this.priority = v; }
    public QueueStatus getStatus() { return status; }
    public int getSequenceNo() { return sequenceNo; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public LocalDateTime getCalledAt() { return calledAt; }
    public LocalDateTime getDoneAt() { return doneAt; }
    public String getCounterNo() { return counterNo; }
    public int getRecallCount() { return recallCount; }
}
