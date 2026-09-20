package com.clinic.domain.queue.strategy;

import com.clinic.domain.queue.QueueTicket;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/** มาก่อนได้ก่อน — ใช้ในคลินิกที่ไม่มีระบบนัด */
@Component
public class FifoQueueStrategy implements QueueOrderingStrategy {

    @Override public String name() { return "FIFO"; }

    @Override
    public List<QueueTicket> order(List<QueueTicket> waitingTickets) {
        return waitingTickets.stream()
                .sorted(Comparator.comparing(QueueTicket::getIssuedAt)
                        .thenComparingInt(QueueTicket::getSequenceNo))
                .toList();
    }
}
