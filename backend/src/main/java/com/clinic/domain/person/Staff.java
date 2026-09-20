package com.clinic.domain.person;

import com.clinic.domain.common.ContactInfo;
import com.clinic.domain.common.Gender;
import jakarta.persistence.*;

import java.time.LocalDate;

/** เจ้าหน้าที่คลินิก — subclass ของ Person */
@Entity
@Table(name = "staff")
@DiscriminatorValue("STAFF")
@PrimaryKeyJoinColumn(name = "person_id")
public class Staff extends Person {

    @Column(name = "staff_code", nullable = false, unique = true, length = 20)
    private String staffCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_role", nullable = false, length = 20)
    private StaffRole staffRole;

    protected Staff() { }

    public Staff(String staffCode, StaffRole staffRole, String firstName, String lastName,
                 Gender gender, LocalDate birthDate, String nationalId, ContactInfo contact) {
        super(firstName, lastName, gender, birthDate, nationalId, contact);
        this.staffCode = staffCode;
        this.staffRole = staffRole;
    }

    @Override
    public String getRoleName() { return "STAFF_" + staffRole.name(); }

    /** สิทธิ์ในการยกเลิกนัดของผู้ป่วยคนอื่น */
    public boolean canCancelAppointment() {
        return staffRole == StaffRole.RECEPTIONIST || staffRole == StaffRole.ADMIN;
    }

    public boolean canManageQueue() {
        return staffRole != StaffRole.CASHIER;
    }

    public String getStaffCode() { return staffCode; }
    public void setStaffCode(String v) { this.staffCode = v; }
    public StaffRole getStaffRole() { return staffRole; }
    public void setStaffRole(StaffRole v) { this.staffRole = v; }
}
