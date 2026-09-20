package com.clinic.domain.person;

import com.clinic.domain.common.BaseEntity;
import com.clinic.domain.common.ContactInfo;
import com.clinic.domain.common.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.Period;

/**
 * Abstract class ของบุคคลทุกประเภทในระบบ (Patient / Doctor / Staff)
 * ใช้ JOINED inheritance: ตาราง person เก็บข้อมูลร่วม, ตารางลูกเก็บข้อมูลเฉพาะ
 */
@Entity
@Table(name = "person")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "person_type", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Person extends BaseEntity {

    @NotBlank
    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "national_id", length = 13, unique = true)
    private String nationalId;

    @Embedded
    private ContactInfo contact;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected Person() { }

    protected Person(String firstName, String lastName, Gender gender,
                     LocalDate birthDate, String nationalId, ContactInfo contact) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.nationalId = nationalId;
        this.contact = contact;
    }

    /** Polymorphism: ลูกแต่ละคลาสบอกบทบาทของตัวเอง */
    public abstract String getRoleName();

    /** ป้ายชื่อที่ใช้แสดงผล — ลูกคลาส override ได้ (เช่น หมอเติมคำนำหน้า นพ.) */
    public String getDisplayName() {
        return getFullName();
    }

    public String getFullName() { return firstName + " " + lastName; }

    public Integer getAge() {
        return birthDate == null ? null : Period.between(birthDate, LocalDate.now()).getYears();
    }

    public boolean isElderly() {
        Integer age = getAge();
        return age != null && age >= 60;
    }

    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public Gender getGender() { return gender; }
    public void setGender(Gender v) { this.gender = v; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate v) { this.birthDate = v; }
    public String getNationalId() { return nationalId; }
    public void setNationalId(String v) { this.nationalId = v; }
    public ContactInfo getContact() { return contact; }
    public void setContact(ContactInfo v) { this.contact = v; }
    public boolean isActive() { return active; }
}
