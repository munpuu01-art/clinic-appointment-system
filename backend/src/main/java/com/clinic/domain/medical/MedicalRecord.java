package com.clinic.domain.medical;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.common.BaseEntity;
import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.person.Patient;
import jakarta.persistence.*;

import java.time.LocalDate;

/** เวชระเบียนของการเข้าตรวจ 1 ครั้ง (ผูก 1:1 กับ Appointment) */
@Entity
@Table(name = "medical_record")
public class MedicalRecord extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "chief_complaint", length = 500)
    private String chiefComplaint;

    @Column(name = "diagnosis", length = 500)
    private String diagnosis;

    @Column(name = "treatment", length = 1000)
    private String treatment;

    @Column(name = "prescription", length = 1000)
    private String prescription;

    @Embedded
    private Vitals vitals;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    protected MedicalRecord() { }

    public MedicalRecord(Appointment appointment, String chiefComplaint) {
        this.appointment = appointment;
        this.patient = appointment.getPatient();
        this.doctor = appointment.getDoctor();
        this.chiefComplaint = chiefComplaint;
        appointment.attachMedicalRecord(this);
    }

    public boolean needsFollowUp() { return followUpDate != null; }

    public Appointment getAppointment() { return appointment; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String v) { this.chiefComplaint = v; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String v) { this.diagnosis = v; }
    public String getTreatment() { return treatment; }
    public void setTreatment(String v) { this.treatment = v; }
    public String getPrescription() { return prescription; }
    public void setPrescription(String v) { this.prescription = v; }
    public Vitals getVitals() { return vitals; }
    public void setVitals(Vitals v) { this.vitals = v; }
    public LocalDate getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDate v) { this.followUpDate = v; }
}
