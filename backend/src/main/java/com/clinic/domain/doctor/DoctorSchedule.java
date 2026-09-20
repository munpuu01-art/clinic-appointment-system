package com.clinic.domain.doctor;

import com.clinic.domain.common.BaseEntity;
import com.clinic.domain.common.TimeSlot;
import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ตารางออกตรวจประจำสัปดาห์ของแพทย์ 1 ช่วง
 * เช่น จันทร์ 09:00-12:00 ช่องละ 20 นาที ห้อง A1
 */
@Entity
@Table(name = "doctor_schedule",
       uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "day_of_week", "start_time"}))
public class DoctorSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_minutes", nullable = false)
    private int slotMinutes = 20;

    /** จำนวนผู้ป่วยต่อ 1 ช่อง (overbooking ได้เล็กน้อยตามนโยบายคลินิก) */
    @Column(name = "capacity_per_slot", nullable = false)
    private int capacityPerSlot = 1;

    @Column(name = "room_no", length = 10)
    private String roomNo;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom = LocalDate.now();

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected DoctorSchedule() { }

    public DoctorSchedule(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                          int slotMinutes, String roomNo) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("เวลาสิ้นสุดต้องมากกว่าเวลาเริ่มต้น");
        }
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotMinutes = slotMinutes;
        this.roomNo = roomNo;
    }

    void assignTo(Doctor doctor) { this.doctor = doctor; }

    /** ตารางนี้ใช้กับวันที่นี้หรือไม่ */
    public boolean appliesOn(LocalDate date) {
        if (!active) return false;
        if (date.getDayOfWeek() != dayOfWeek) return false;
        if (date.isBefore(effectiveFrom)) return false;
        return effectiveTo == null || !date.isAfter(effectiveTo);
    }

    public boolean covers(TimeSlot slot) {
        return asTimeSlot().contains(slot);
    }

    public TimeSlot asTimeSlot() { return new TimeSlot(startTime, endTime); }

    /** สร้างช่องเวลาย่อยทั้งหมดของตารางนี้ */
    public List<TimeSlot> generateSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime cursor = startTime;
        while (!cursor.plusMinutes(slotMinutes).isAfter(endTime)) {
            slots.add(TimeSlot.of(cursor, slotMinutes));
            cursor = cursor.plusMinutes(slotMinutes);
        }
        return slots;
    }

    public int totalCapacity() { return generateSlots().size() * capacityPerSlot; }

    public void deactivate() { this.active = false; }

    public Doctor getDoctor() { return doctor; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek v) { this.dayOfWeek = v; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime v) { this.startTime = v; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime v) { this.endTime = v; }
    public int getSlotMinutes() { return slotMinutes; }
    public void setSlotMinutes(int v) { this.slotMinutes = v; }
    public int getCapacityPerSlot() { return capacityPerSlot; }
    public void setCapacityPerSlot(int v) { this.capacityPerSlot = v; }
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String v) { this.roomNo = v; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate v) { this.effectiveFrom = v; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate v) { this.effectiveTo = v; }
    public boolean isActive() { return active; }
}
