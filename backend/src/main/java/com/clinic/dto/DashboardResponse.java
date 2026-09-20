package com.clinic.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        LocalDate date,
        long totalAppointments,
        long confirmed,
        long checkedIn,
        long completed,
        long cancelled,
        long noShow,
        long waitingInQueue,
        List<DoctorLoad> doctorLoads,
        Map<String, Long> eventCounters
) {
    public record DoctorLoad(Long doctorId, String doctorName, long appointmentCount, int waitingCount) { }
}
