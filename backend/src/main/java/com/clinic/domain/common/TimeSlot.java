package com.clinic.domain.common;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Value Object: ช่วงเวลา 1 ช่อง (immutable)
 * ใช้ตรวจการชนกันของเวลานัดหมาย
 */
public final class TimeSlot implements Comparable<TimeSlot> {

    private final LocalTime start;
    private final LocalTime end;

    public TimeSlot(LocalTime start, LocalTime end) {
        if (start == null || end == null) throw new IllegalArgumentException("ต้องระบุเวลาเริ่มและสิ้นสุด");
        if (!end.isAfter(start)) throw new IllegalArgumentException("เวลาสิ้นสุดต้องมากกว่าเวลาเริ่ม");
        this.start = start;
        this.end = end;
    }

    public static TimeSlot of(LocalTime start, int minutes) {
        return new TimeSlot(start, start.plusMinutes(minutes));
    }

    public LocalTime getStart() { return start; }
    public LocalTime getEnd() { return end; }

    public int durationMinutes() { return (int) Duration.between(start, end).toMinutes(); }

    /** ชนกันหรือไม่ (ใช้กฎ half-open [start, end) ) */
    public boolean overlaps(TimeSlot other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    public boolean contains(TimeSlot other) {
        return !other.start.isBefore(start) && !other.end.isAfter(end);
    }

    @Override
    public int compareTo(TimeSlot o) { return start.compareTo(o.start); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot t)) return false;
        return start.equals(t.start) && end.equals(t.end);
    }

    @Override
    public int hashCode() { return start.hashCode() * 31 + end.hashCode(); }

    @Override
    public String toString() { return start + "-" + end; }
}
