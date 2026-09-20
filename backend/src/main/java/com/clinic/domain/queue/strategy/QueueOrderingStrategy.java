package com.clinic.domain.queue.strategy;

import com.clinic.domain.queue.QueueTicket;
import java.util.List;

/**
 * Strategy Pattern: อัลกอริทึมจัดลำดับคิว สลับได้โดยไม่แก้ QueueService
 */
public interface QueueOrderingStrategy {
    String name();
    List<QueueTicket> order(List<QueueTicket> waitingTickets);
}
