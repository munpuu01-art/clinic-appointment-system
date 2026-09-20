package com.clinic.domain.doctor;

import com.clinic.domain.common.BaseEntity;
import com.clinic.domain.common.TimeSlot;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

/** วันลา / งดออกตรวจของแพทย์ (ข้อยกเว้นที่ทับตารางปกติ) */
@Entity
@Table(name = "doctor_leave")
public class DoctorLeave extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    @Column(name = "full_day", nullable = false)
    private boolean fullDay = true;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "reason", length = 200)
    private String reason;

    protected DoctorLeave() { }

    public static DoctorLeave fullDay(LocalDate date, String reason) {
        DoctorLeave l = new DoctorLeave();
        l.leaveDate = date;
        l.fullDay = true;
        l.reason = reason;
        return l;
    }

    public static DoctorLeave partial(LocalDate date, LocalTime start, LocalTime end, String reason) {
        DoctorLeave l = new DoctorLeave();
        l.leaveDate = date;
        l.fullDay = false;
        l.startTime = start;
        l.endTime = end;
        l.reason = reason;
        return l;
    }

    void assignTo(Doctor doctor) { this.doctor = doctor; }

    /** ช่วงเวลานี้ถูกบล็อกด้วยวันลาหรือไม่ */
    public boolean blocks(LocalDate date, TimeSlot slot) {
        if (!leaveDate.equals(date)) return false;
        if (fullDay) return true;
        return new TimeSlot(startTime, endTime).overlaps(slot);
    }

    public Doctor getDoctor() { return doctor; }
    public LocalDate getLeaveDate() { return leaveDate; }
    public boolean isFullDay() { return fullDay; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getReason() { return reason; }
}
