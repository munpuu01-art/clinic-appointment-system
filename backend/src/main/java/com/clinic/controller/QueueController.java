package com.clinic.controller;

import com.clinic.dto.CallQueueRequest;
import com.clinic.dto.QueueBoardResponse;
import com.clinic.dto.QueueTicketResponse;
import com.clinic.dto.WalkInRequest;
import com.clinic.service.QueueService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) { this.queueService = queueService; }

    /** จอแสดงคิวของห้องตรวจ */
    @GetMapping("/board")
    public QueueBoardResponse board(@RequestParam Long doctorId,
                                    @RequestParam(required = false)
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                    @RequestParam(required = false) String strategy) {
        return queueService.board(doctorId, date != null ? date : LocalDate.now(), strategy);
    }

    @PostMapping("/walk-in")
    public ResponseEntity<QueueTicketResponse> walkIn(@Valid @RequestBody WalkInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(queueService.registerWalkIn(request));
    }

    @PostMapping("/call-next")
    public QueueTicketResponse callNext(@RequestParam Long doctorId,
                                        @RequestBody(required = false) CallQueueRequest request) {
        return queueService.callNext(doctorId, request);
    }

    @PatchMapping("/tickets/{ticketId}/recall")
    public QueueTicketResponse recall(@PathVariable Long ticketId) { return queueService.recall(ticketId); }

    @PatchMapping("/tickets/{ticketId}/serve")
    public QueueTicketResponse serve(@PathVariable Long ticketId) { return queueService.serve(ticketId); }

    @PatchMapping("/tickets/{ticketId}/complete")
    public QueueTicketResponse complete(@PathVariable Long ticketId) { return queueService.complete(ticketId); }

    @PatchMapping("/tickets/{ticketId}/skip")
    public QueueTicketResponse skip(@PathVariable Long ticketId) { return queueService.skip(ticketId); }

    @PatchMapping("/tickets/{ticketId}/requeue")
    public QueueTicketResponse requeue(@PathVariable Long ticketId) { return queueService.requeue(ticketId); }

    @GetMapping("/strategies")
    public List<String> strategies() { return queueService.availableStrategies(); }
}
