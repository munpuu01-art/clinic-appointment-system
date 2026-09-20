package com.clinic.service.impl;

import com.clinic.domain.appointment.AppointmentStatus;
import com.clinic.domain.queue.QueueStatus;
import com.clinic.domain.queue.QueueTicket;
import com.clinic.dto.DashboardResponse;
import com.clinic.event.StatisticsObserver;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.QueueTicketRepository;
import com.clinic.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final QueueTicketRepository ticketRepository;
    private final DoctorRepository doctorRepository;
    private final StatisticsObserver statisticsObserver;

    public DashboardServiceImpl(AppointmentRepository appointmentRepository,
                                QueueTicketRepository ticketRepository,
                                DoctorRepository doctorRepository,
                                StatisticsObserver statisticsObserver) {
        this.appointmentRepository = appointmentRepository;
        this.ticketRepository = ticketRepository;
        this.doctorRepository = doctorRepository;
        this.statisticsObserver = statisticsObserver;
    }

    @Override
    public DashboardResponse summary(LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();

        List<DashboardResponse.DoctorLoad> loads = doctorRepository.findByActiveTrue().stream()
                .map(d -> {
                    long appointments = appointmentRepository
                            .findByDoctorIdAndAppointmentDate(d.getId(), target).size();
                    int waiting = (int) ticketRepository.findByDoctorIdAndQueueDate(d.getId(), target)
                            .stream().filter(QueueTicket::isWaiting).count();
                    return new DashboardResponse.DoctorLoad(d.getId(), d.getDisplayName(), appointments, waiting);
                })
                .toList();

        long waitingInQueue = ticketRepository.findByQueueDateAndStatus(target, QueueStatus.WAITING).size();

        return new DashboardResponse(
                target,
                appointmentRepository.countByAppointmentDate(target),
                appointmentRepository.countByAppointmentDateAndStatus(target, AppointmentStatus.CONFIRMED),
                appointmentRepository.countByAppointmentDateAndStatus(target, AppointmentStatus.CHECKED_IN),
                appointmentRepository.countByAppointmentDateAndStatus(target, AppointmentStatus.COMPLETED),
                appointmentRepository.countByAppointmentDateAndStatus(target, AppointmentStatus.CANCELLED),
                appointmentRepository.countByAppointmentDateAndStatus(target, AppointmentStatus.NO_SHOW),
                waitingInQueue,
                loads,
                statisticsObserver.snapshot()
        );
    }
}
