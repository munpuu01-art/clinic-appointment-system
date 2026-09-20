package com.clinic.domain;

import com.clinic.domain.common.TimeSlot;
import com.clinic.domain.doctor.DoctorSchedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** TC-06..TC-08 : ทดสอบช่วงเวลาและการสร้างช่องนัด */
class TimeSlotTest {

    @Test
    @DisplayName("TC-06 ช่วงเวลาที่คาบเกี่ยวกันต้องตรวจพบว่าชนกัน")
    void overlapDetection() {
        TimeSlot a = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 20));
        TimeSlot b = new TimeSlot(LocalTime.of(9, 10), LocalTime.of(9, 30));
        TimeSlot c = new TimeSlot(LocalTime.of(9, 20), LocalTime.of(9, 40));

        assertTrue(a.overlaps(b));
        assertFalse(a.overlaps(c), "ช่องต่อเนื่องกันพอดีต้องไม่ถือว่าชน");
    }

    @Test
    @DisplayName("TC-07 เวลาสิ้นสุดต้องมากกว่าเวลาเริ่ม")
    void invalidRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new TimeSlot(LocalTime.of(10, 0), LocalTime.of(9, 0)));
    }

    @Test
    @DisplayName("TC-08 ตาราง 09:00-12:00 ช่องละ 20 นาที ต้องได้ 9 ช่อง")
    void generateSlots() {
        DoctorSchedule schedule = new DoctorSchedule(DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0), 20, "A1");
        List<TimeSlot> slots = schedule.generateSlots();

        assertEquals(9, slots.size());
        assertEquals(LocalTime.of(9, 0), slots.get(0).getStart());
        assertEquals(LocalTime.of(11, 40), slots.get(slots.size() - 1).getStart());
    }
}
