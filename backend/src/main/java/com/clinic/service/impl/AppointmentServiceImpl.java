package com.clinic.service.impl;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentType;
import com.clinic.domain.common.TimeSlot;
import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.doctor.DoctorSchedule;
import com.clinic.domain.person.Patient;
import com.clinic.domain.pricing.FeeCalculator;
import com.clinic.dto.*;
import com.clinic.event.AppointmentEvent;
import com.clinic.event.AppointmentEventPublisher;
import com.clinic.event.AppointmentEventType;
import com.clinic.exception.BusinessRuleException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.factory.AppointmentFactory;
import com.clinic.mapper.DomainMapper;
import com.clinic.repository.AppointmentRepository;
import com.clinic.service.AppointmentService;
import com.clinic.service.DoctorService;
import com.clinic.service.PatientService;
import com.clinic.service.QueueService;
import com.clinic.rules.BookingContext;
import com.clinic.rules.BookingRuleChain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * หัวใจของระบบ: ประสานงาน Domain + Rule Chain + Factory + Observer
 * สังเกตว่าไม่มี if-else ของ workflow อยู่ที่นี่ — ทั้งหมดอยู่ในคลาส State
 */
@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final QueueService queueService;
    private final AppointmentFactory appointmentFactory;
    private final BookingRuleChain ruleChain;
    private final FeeCalculator feeCalculator;
    private final AppointmentEventPublisher eventPublisher;
    private final DomainMapper mapper;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  PatientService patientService,
                                  DoctorService doctorService,
                                  QueueService queueService,
                                  AppointmentFactory appointmentFactory,
                                  BookingRuleChain ruleChain,
                                  FeeCalculator feeCalculator,
                                  AppointmentEventPublisher eventPublisher,
                                  DomainMapper mapper) {
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.queueService = queueService;
        this.appointmentFactory = appointmentFactory;
        this.ruleChain = ruleChain;
        this.feeCalculator = feeCalculator;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @Override
    public AppointmentResponse book(BookAppointmentRequest r) {
        Patient patient = patientService.getEntity(r.patientId());
        Doctor doctor = doctorService.getEntity(r.doctorId());

        if (r.type() == AppointmentType.WALK_IN) {
            throw new BusinessRuleException("USE_WALKIN_API",
                    "ผู้ป่วย walk-in ให้ลงทะเบียนผ่านเมนูคิวหน้างาน");
        }

        DoctorSchedule schedule = findScheduleFor(doctor, r.date(), r.startTime());
        TimeSlot slot = TimeSlot.of(r.startTime(), schedule.getSlotMinutes());

        // 1) ตรวจกฎธุรกิจทั้งโซ่ ก่อนสร้างข้อมูลใด ๆ
        ruleChain.validate(BookingContext.of(patient, doctor, r.date(), slot, r.type()));

        // 2) ให้ Factory ประกอบ Appointment (รวมเลขที่นัดและค่าบริการ)
        Appointment appointment = appointmentFactory.createScheduled(
                patient, doctor, schedule, r.date(), slot, r.type(), r.symptomNote(), r.createdBy());

        Appointment saved = appointmentRepository.save(appointment);

        // 3) ประกาศเหตุการณ์ให้ผู้สังเกตทุกตัว (แจ้งเตือน / audit / สถิติ)
        eventPublisher.publish(AppointmentEvent.of(AppointmentEventType.BOOKED, saved));
        return mapper.toDto(saved);
    }

    @Override
    public AppointmentResponse confirm(Long id) {
        Appointment a = getEntity(id);
        a.confirm();
        Appointment saved = appointmentRepository.save(a);
        eventPublisher.publish(AppointmentEvent.of(AppointmentEventType.CONFIRMED, saved));
        return mapper.toDto(saved);
    }

    @Override
    public AppointmentResponse reschedule(Long id, RescheduleRequest r) {
        Appointment a = getEntity(id);
        Doctor doctor = a.getDoctor();

        DoctorSchedule schedule = findScheduleFor(doctor, r.date(), r.startTime());
        TimeSlot slot = TimeSlot.of(r.startTime(), schedule.getSlotMinutes());

        BookingContext ctx = new BookingContext(a.getPatient(), doctor, r.date(), slot, a.getType(), a.getId());
        ruleChain.validate(ctx);

        a.reschedule(r.date(), r.startTime(), schedule);
        a.applyFee(feeCalculator.calculate(a));

        Appointment saved = appointmentRepository.save(a);
        eventPublisher.publish(AppointmentEvent.of(AppointmentEventType.RESCHEDULED, saved, r.reason()));
        return mapper.toDto(saved);
    }

    @Override
    public AppointmentResponse cancel(Long id, CancelRequest r) {
        Appointment a = getEntity(id);
        a.cancel(r.reason());
        Appointment saved = appointmentRepository.save(a);
        eventPublisher.publish(AppointmentEvent.of(AppointmentEventType.CANCELLED, saved, r.reason()));
        return mapper.toDto(saved);
    }

    /** เช็คอิน = เปลี่ยนสถานะนัด + ออกบัตรคิวอัตโนมัติ (ทำใน transaction เดียว) */
    @Override
    public AppointmentResponse checkIn(Long id) {
        Appointment a = getEntity(id);
        a.checkIn();
        Appointment saved = appointmentRepository.save(a);
        queueService.issueTicketForAppointment(saved.getId());
        eventPublisher.publish(AppointmentEvent.of(AppointmentEventType.CHECKED_IN, saved));
        return mapper.toDto(getEntity(id));
    }

    @Override
    public AppointmentResponse startExam(Long id) {
        Appointment a = getEntity(id);
        a.start();
        return mapper.toDto(appointmentRepository.save(a));
    }

    @Override
    public AppointmentResponse complete(Long id) {
        Appointment a = getEntity(id);
        a.complete();
        Appointment saved = appointmentRepository.save(a);
        eventPublisher.publish(AppointmentEvent.of(AppointmentEventType.COMPLETED, saved));
        return mapper.toDto(saved);
    }

    @Override
    public AppointmentResponse markNoShow(Long id) {
        Appointment a = getEntity(id);
        a.markNoShow();
        Appointment saved = appointmentRepository.save(a);
        eventPublisher.publish(AppointmentEvent.of(AppointmentEventType.NO_SHOW, saved));
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse findById(Long id) { return mapper.toDto(getEntity(id)); }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findByDate(LocalDate date, Pageable pageable) {
        return appointmentRepository.findByAppointmentDate(date, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(patientId)
                .stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Appointment getEntity(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("นัดหมาย", id));
    }

    private DoctorSchedule findScheduleFor(Doctor doctor, LocalDate date, LocalTime startTime) {
        return doctor.getSchedules().stream()
                .filter(s -> s.appliesOn(date))
                .filter(s -> !startTime.isBefore(s.getStartTime()) && startTime.isBefore(s.getEndTime()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("NO_SCHEDULE",
                        "แพทย์ไม่มีตารางออกตรวจในวันและเวลาที่เลือก"));
    }
}
