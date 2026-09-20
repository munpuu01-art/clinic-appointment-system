package com.clinic.event;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Observer ที่นับสถิติแบบเรียลไทม์ให้หน้า Dashboard */
@Component
public class StatisticsObserver implements AppointmentObserver {

    private final Map<AppointmentEventType, AtomicLong> counters = new ConcurrentHashMap<>();

    @Override public String observerName() { return "STATISTICS"; }

    @Override public boolean supports(AppointmentEventType type) { return true; }

    @Override
    public void onEvent(AppointmentEvent event) {
        counters.computeIfAbsent(event.type(), k -> new AtomicLong()).incrementAndGet();
    }

    public Map<String, Long> snapshot() {
        Map<String, Long> result = new java.util.LinkedHashMap<>();
        counters.forEach((k, v) -> result.put(k.name(), v.get()));
        return result;
    }
}
