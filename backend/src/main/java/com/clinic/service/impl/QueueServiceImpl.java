package com.clinic.service.impl;

import com.clinic.config.ClinicProperties;
import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.person.Patient;
import com.clinic.domain.queue.*;
import com.clinic.domain.queue.strategy.QueueOrderingStrategy;
import com.clinic.domain.queue.strategy.QueueStrategyFactory;
import com.clinic.dto.CallQueueRequest;
import com.clinic.dto.QueueBoardResponse;
import com.clinic.dto.QueueTicketResponse;
import com.clinic.dto.WalkInRequest;
import com.clinic.event.AppointmentEvent;
import com.clinic.event.AppointmentEventPublisher;
import com.clinic.event.AppointmentEventType;
import com.clinic.exception.BusinessRuleException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.factory.AppointmentFactory;
import com.clinic.factory.DocumentNumberGenerator;
import com.clinic.mapper.DomainMapper;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.repository.QueueTicketRepository;
import com.clinic.service.QueueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class QueueServiceImpl implements QueueService {

    private final QueueTicketRepository ticketRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentFactory appointmentFactory;
    private final DocumentNumberGenerator numberGenerator;
    private final QueueStrategyFactory strategyFactory;
    private final AppointmentEventPublisher eventPublisher;
    private final ClinicProperties properties;
    private final DomainMapper mapper;

    public QueueServiceImpl(QueueTicketRepository ticketRepository,
                            AppointmentRepository appointmentRepository,
                            PatientRepository patientRepository,
                            DoctorRepository doctorRepository,
                            AppointmentFactory appointmentFactory,
                            DocumentNumberGenerator numberGenerator,
                            QueueStrategyFactory strategyFactory,
                            AppointmentEventPublisher eventPublisher,
                            ClinicProperties properties,
                            DomainMapper mapper) {
        this.ticketRepository = ticketRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentFactory = appointmentFactory;
        this.numberGenerator = numberGenerator;
        this.strategyFactory = strategyFactory;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    public QueueTicketResponse issueTicketForAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("นัดหมาย", appointmentId));

        ticketRepository.findByAppointmentId(appointmentId).ifPresent(t -> {
            throw new BusinessRuleException("TICKET_EXISTS", "นัดหมายนี้ออกบัตรคิวไปแล้ว: " + t.getTicketNo());
        });

        QueuePriority priority = appointment.getPatient().isElderly()
                ? QueuePriority.ELDERLY : QueuePriority.APPOINTMENT;

        QueueTicket ticket = createTicket(appointment.getDoctor(), appointment.getPatient(),
                appointment.getAppointmentDate(), priority);
        ticket.linkTo(appointment);

        return mapper.toDto(ticketRepository.save(ticket));
    }

    /** ผู้ป่วย walk-in: สร้างนัดหมายให้อัตโนมัติ แล้วออกบัตรคิวต่อทันที */
    @Override
    public QueueTicketResponse registerWalkIn(WalkInRequest r) {
        Patient patient = patientRepository.findById(r.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("ผู้ป่วย", r.patientId()));
        Doctor doctor = doctorRepository.findById(r.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("แพทย์", r.doctorId()));

        LocalDate today = LocalDate.now();
        boolean alreadyInQueue = ticketRepository.existsByPatientIdAndQueueDateAndStatusIn(
                patient.getId(), today, List.of(QueueStatus.WAITING, QueueStatus.CALLED, QueueStatus.SERVING));
        if (alreadyInQueue) {
            throw new BusinessRuleException("ALREADY_IN_QUEUE", "ผู้ป่วยมีคิวที่ยังไม่เสร็จอยู่แล้ววันนี้");
        }

        Appointment appointment = appointmentFactory.createWalkIn(patient, doctor, r.symptomNote(), r.createdBy());
        appointment.checkIn();
        Appointment savedAppointment = appointmentRepository.save(appointment);

        QueuePriority priority = r.priority() != null ? r.priority()
                : (patient.isElderly() ? QueuePriority.ELDERLY : QueuePriority.NORMAL);

        QueueTicket ticket = createTicket(doctor, patient, today, priority);
        ticket.linkTo(savedAppointment);
        QueueTicket saved = ticketRepository.save(ticket);

        eventPublisher.publish(AppointmentEvent.of(AppointmentEventType.CHECKED_IN, savedAppointment, "walk-in"));
        return mapper.toDto(saved);
    }

    private QueueTicket createTicket(Doctor doctor, Patient patient, LocalDate date, QueuePriority priority) {
        int sequence = ticketRepository.maxSequenceNo(doctor.getId(), date) + 1;
        String ticketNo = numberGenerator.nextTicketNo(date, doctor.getSpecialty().getCode(), sequence);
        return new QueueTicket(ticketNo, date, doctor, patient, priority, sequence);
    }

    @Override
    @Transactional(readOnly = true)
    public QueueBoardResponse board(Long doctorId, LocalDate date, String strategyName) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("แพทย์", doctorId));
        DailyQueue queue = loadQueue(doctor, date, strategyName);

        List<QueueTicketResponse> waiting = queue.waitingOrdered().stream().map(mapper::toDto).toList();

        return new QueueBoardResponse(
                doctor.getId(), doctor.getDisplayName(), doctor.getRoomNo(), date,
                queue.strategyName(),
                queue.currentlyServing().map(mapper::toDto).orElse(null),
                waiting, queue.waitingCount(), queue.doneCount(), queue.estimatedWaitMinutes());
    }

    private DailyQueue loadQueue(Doctor doctor, LocalDate date, String strategyName) {
        QueueOrderingStrategy strategy = strategyFactory.create(
                strategyName != null ? strategyName : properties.getQueue().getDefaultStrategy());
        List<QueueTicket> tickets = ticketRepository.findByDoctorIdAndQueueDate(doctor.getId(), date);
        return new DailyQueue(doctor, date, tickets, strategy);
    }

    @Override
    public QueueTicketResponse callNext(Long doctorId, CallQueueRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("แพทย์", doctorId));
        DailyQueue queue = loadQueue(doctor, LocalDate.now(), null);

        QueueTicket next = queue.peekNext().orElseThrow(() ->
                new BusinessRuleException("QUEUE_EMPTY", "ไม่มีคิวที่รออยู่"));

        next.call(request == null ? doctor.getRoomNo() : request.counterNo());
        QueueTicket saved = ticketRepository.save(next);

        if (saved.getAppointment() != null) {
            eventPublisher.publish(AppointmentEvent.of(
                    AppointmentEventType.QUEUE_CALLED, saved.getAppointment(), "คิว " + saved.getTicketNo()));
        }
        return mapper.toDto(saved);
    }

    @Override
    public QueueTicketResponse recall(Long ticketId) {
        QueueTicket ticket = getTicket(ticketId);
        ticket.recall();
        return mapper.toDto(ticketRepository.save(ticket));
    }

    /** เริ่มรับบริการ: คิวเปลี่ยนเป็น SERVING และนัดหมายเปลี่ยนเป็น IN_PROGRESS พร้อมกัน */
    @Override
    public QueueTicketResponse serve(Long ticketId) {
        QueueTicket ticket = getTicket(ticketId);
        ticket.serve();
        if (ticket.getAppointment() != null) {
            ticket.getAppointment().start();
            appointmentRepository.save(ticket.getAppointment());
        }
        return mapper.toDto(ticketRepository.save(ticket));
    }

    @Override
    public QueueTicketResponse complete(Long ticketId) {
        QueueTicket ticket = getTicket(ticketId);
        ticket.complete();
        if (ticket.getAppointment() != null) {
            Appointment a = ticket.getAppointment();
            a.complete();
            appointmentRepository.save(a);
            eventPublisher.publish(AppointmentEvent.of(AppointmentEventType.COMPLETED, a));
        }
        return mapper.toDto(ticketRepository.save(ticket));
    }

    @Override
    public QueueTicketResponse skip(Long ticketId) {
        QueueTicket ticket = getTicket(ticketId);
        ticket.skip();
        return mapper.toDto(ticketRepository.save(ticket));
    }

    @Override
    public QueueTicketResponse requeue(Long ticketId) {
        QueueTicket ticket = getTicket(ticketId);
        int nextSeq = ticketRepository.maxSequenceNo(ticket.getDoctor().getId(), ticket.getQueueDate()) + 1;
        ticket.requeue(nextSeq);
        return mapper.toDto(ticketRepository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> availableStrategies() { return strategyFactory.availableStrategies(); }

    private QueueTicket getTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("บัตรคิว", id));
    }
}
