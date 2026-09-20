package com.clinic.domain.doctor;

import com.clinic.domain.common.TimeSlot;
import java.time.LocalDate;

/**
 * Interface: สิ่งที่ "รับนัดได้" — ปัจจุบันคือ Doctor
 * อนาคตต่อยอดเป็น Room / Machine (เช่น เครื่อง X-ray) ได้โดยไม่แก้ service
 */
public interface Schedulable {
    boolean isAvailableAt(LocalDate date, TimeSlot slot);
    String resourceName();
}
