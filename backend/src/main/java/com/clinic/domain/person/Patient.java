package com.clinic.domain.person;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.common.Address;
import com.clinic.domain.common.ContactInfo;
import com.clinic.domain.common.Gender;
import com.clinic.domain.medical.MedicalRecord;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** ผู้ป่วย — subclass ของ Person */
@Entity
@Table(name = "patient")
@DiscriminatorValue("PATIENT")
@PrimaryKeyJoinColumn(name = "person_id")
public class Patient extends Person {

    /** HN (Hospital Number) เช่น HN-2026-0001 */
    @Column(name = "hn", nullable = false, unique = true, length = 20)
    private String hn;

    @Embedded
    private Address address;

    @Column(name = "blood_type", length = 5)
    private String bloodType;

    /** รายการแพ้ยา คั่นด้วยเครื่องหมายจุลภาค */
    @Column(name = "allergies", length = 500)
    private String allergies;

    @Column(name = "chronic_disease", length = 500)
    private String chronicDisease;

    @Column(name = "emergency_contact", length = 120)
    private String emergencyContact;

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    protected Patient() { }

    public Patient(String hn, String firstName, String lastName, Gender gender,
                   LocalDate birthDate, String nationalId, ContactInfo contact) {
        super(firstName, lastName, gender, birthDate, nationalId, contact);
        this.hn = hn;
    }

    @Override
    public String getRoleName() { return "PATIENT"; }

    /** ตรวจว่าผู้ป่วยแพ้ยาตัวนี้หรือไม่ (business logic อยู่ใน domain ไม่ใช่ controller) */
    public boolean isAllergicTo(String drug) {
        if (allergies == null || drug == null) return false;
        return Arrays.stream(allergies.split(","))
                .map(String::trim)
                .anyMatch(a -> !a.isEmpty() && a.equalsIgnoreCase(drug.trim()));
    }

    public void addAllergy(String drug) {
        if (drug == null || drug.isBlank() || isAllergicTo(drug)) return;
        this.allergies = (allergies == null || allergies.isBlank()) ? drug : allergies + ", " + drug;
    }

    public boolean isNewPatient() { return appointments.isEmpty(); }

    public String getHn() { return hn; }
    public void setHn(String v) { this.hn = v; }
    public Address getAddress() { return address; }
    public void setAddress(Address v) { this.address = v; }
    public String getBloodType() { return bloodType; }
    public void setBloodType(String v) { this.bloodType = v; }
    public String getAllergies() { return allergies; }
    public void setAllergies(String v) { this.allergies = v; }
    public String getChronicDisease() { return chronicDisease; }
    public void setChronicDisease(String v) { this.chronicDisease = v; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String v) { this.emergencyContact = v; }
    public List<Appointment> getAppointments() { return appointments; }
    public List<MedicalRecord> getMedicalRecords() { return medicalRecords; }
}
