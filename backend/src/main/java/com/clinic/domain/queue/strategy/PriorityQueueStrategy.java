package com.clinic.domain.queue.strategy;

import com.clinic.domain.queue.QueueTicket;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/** ฉุกเฉิน > ผู้สูงอายุ > มีนัด > ทั่วไป ภายในระดับเดียวกันใช้มาก่อนได้ก่อน */
@Component
public class PriorityQueueStrategy implements QueueOrderingStrategy {

    @Override public String name() { return "PRIORITY"; }

    @Override
    public List<QueueTicket> order(List<QueueTicket> waitingTickets) {
        return waitingTickets.stream()
                .sorted(Comparator.comparingInt((QueueTicket t) -> t.getPriority().getWeight())
                        .thenComparing(QueueTicket::getIssuedAt)
                        .thenComparingInt(QueueTicket::getSequenceNo))
                .toList();
    }
}
