package com.clinic.domain.doctor;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.common.ContactInfo;
import com.clinic.domain.common.Gender;
import com.clinic.domain.common.TimeSlot;
import com.clinic.domain.person.Person;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** แพทย์ — subclass ของ Person และเป็นทรัพยากรที่นัดหมายได้ (Schedulable) */
@Entity
@Table(name = "doctor")
@DiscriminatorValue("DOCTOR")
@PrimaryKeyJoinColumn(name = "person_id")
public class Doctor extends Person implements Schedulable {

    @Column(name = "license_no", nullable = false, unique = true, length = 20)
    private String licenseNo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(name = "room_no", length = 10)
    private String roomNo;

    @Column(name = "biography", length = 1000)
    private String biography;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DoctorSchedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DoctorLeave> leaves = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    protected Doctor() { }

    public Doctor(String licenseNo, Specialty specialty, String firstName, String lastName,
                  Gender gender, LocalDate birthDate, String nationalId, ContactInfo contact) {
        super(firstName, lastName, gender, birthDate, nationalId, contact);
        this.licenseNo = licenseNo;
        this.specialty = specialty;
    }

    @Override
    public String getRoleName() { return "DOCTOR"; }

    /** Polymorphism: แพทย์แสดงชื่อพร้อมคำนำหน้าและแผนก */
    @Override
    public String getDisplayName() {
        return "นพ./พญ. " + getFullName() + " (" + specialty.getName() + ")";
    }

    @Override
    public String resourceName() { return "DOCTOR#" + getId(); }

    /** ว่างในวัน/ช่วงเวลานี้ไหม = มีตารางออกตรวจครอบคลุม และไม่ติดวันลา */
    @Override
    public boolean isAvailableAt(LocalDate date, TimeSlot slot) {
        if (!isActive()) return false;
        boolean inSchedule = schedules.stream()
                .anyMatch(s -> s.appliesOn(date) && s.covers(slot));
        if (!inSchedule) return false;
        return leaves.stream().noneMatch(l -> l.blocks(date, slot));
    }

    /** ค่าตรวจ: ถ้าไม่ได้ตั้งค่าเฉพาะคน ใช้ค่ากลางของแผนก */
    public BigDecimal effectiveFee() {
        return consultationFee != null ? consultationFee : specialty.getBaseFee();
    }

    public void addSchedule(DoctorSchedule schedule) {
        schedule.assignTo(this);
        this.schedules.add(schedule);
    }

    public void removeSchedule(DoctorSchedule schedule) {
        this.schedules.remove(schedule);
    }

    public void addLeave(DoctorLeave leave) {
        leave.assignTo(this);
        this.leaves.add(leave);
    }

    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String v) { this.licenseNo = v; }
    public Specialty getSpecialty() { return specialty; }
    public void setSpecialty(Specialty v) { this.specialty = v; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal v) { this.consultationFee = v; }
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String v) { this.roomNo = v; }
    public String getBiography() { return biography; }
    public void setBiography(String v) { this.biography = v; }
    public List<DoctorSchedule> getSchedules() { return schedules; }
    public List<DoctorLeave> getLeaves() { return leaves; }
    public List<Appointment> getAppointments() { return appointments; }
}
