package com.clinic.service.impl;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentStatus;
import com.clinic.domain.medical.MedicalRecord;
import com.clinic.domain.medical.Vitals;
import com.clinic.dto.MedicalRecordRequest;
import com.clinic.dto.MedicalRecordResponse;
import com.clinic.exception.BusinessRuleException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.DomainMapper;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.MedicalRecordRepository;
import com.clinic.service.MedicalRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository recordRepository;
    private final AppointmentRepository appointmentRepository;
    private final DomainMapper mapper;

    public MedicalRecordServiceImpl(MedicalRecordRepository recordRepository,
                                    AppointmentRepository appointmentRepository,
                                    DomainMapper mapper) {
        this.recordRepository = recordRepository;
        this.appointmentRepository = appointmentRepository;
        this.mapper = mapper;
    }

    /** บันทึกผลตรวจได้เฉพาะนัดที่กำลังตรวจหรือตรวจเสร็จแล้ว */
    @Override
    public MedicalRecordResponse saveForAppointment(Long appointmentId, MedicalRecordRequest r) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("นัดหมาย", appointmentId));

        if (appointment.getStatus() != AppointmentStatus.IN_PROGRESS
                && appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException("RECORD_NOT_ALLOWED",
                    "บันทึกผลตรวจได้เมื่อเริ่มตรวจแล้วเท่านั้น (สถานะปัจจุบัน: "
                            + appointment.getStatus().getLabel() + ")");
        }

        MedicalRecord record = recordRepository.findByAppointmentId(appointmentId)
                .orElseGet(() -> new MedicalRecord(appointment, r.chiefComplaint()));

        record.setChiefComplaint(r.chiefComplaint());
        record.setDiagnosis(r.diagnosis());
        record.setTreatment(r.treatment());
        record.setPrescription(r.prescription());
        record.setFollowUpDate(r.followUpDate());
        record.setVitals(new Vitals(r.temperatureC(), r.systolic(), r.diastolic(),
                r.pulse(), r.weightKg(), r.heightCm()));

        return mapper.toDto(recordRepository.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordResponse findByAppointment(Long appointmentId) {
        return recordRepository.findByAppointmentId(appointmentId).map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("เวชระเบียนของนัดหมาย", appointmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> historyOfPatient(Long patientId) {
        return recordRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(mapper::toDto).toList();
    }
}
