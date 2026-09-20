package com.clinic.service;

import com.clinic.dto.CallQueueRequest;
import com.clinic.dto.QueueBoardResponse;
import com.clinic.dto.QueueTicketResponse;
import com.clinic.dto.WalkInRequest;

import java.time.LocalDate;
import java.util.List;

public interface QueueService {
    QueueTicketResponse issueTicketForAppointment(Long appointmentId);
    QueueTicketResponse registerWalkIn(WalkInRequest request);
    QueueBoardResponse board(Long doctorId, LocalDate date, String strategy);
    QueueTicketResponse callNext(Long doctorId, CallQueueRequest request);
    QueueTicketResponse recall(Long ticketId);
    QueueTicketResponse serve(Long ticketId);
    QueueTicketResponse complete(Long ticketId);
    QueueTicketResponse skip(Long ticketId);
    QueueTicketResponse requeue(Long ticketId);
    List<String> availableStrategies();
}
