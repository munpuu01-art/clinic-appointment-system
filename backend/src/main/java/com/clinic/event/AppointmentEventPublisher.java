package com.clinic.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Subject ของ Observer Pattern — กระจายเหตุการณ์ให้ผู้สังเกตทุกตัว */
@Component
public class AppointmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEventPublisher.class);

    private final List<AppointmentObserver> observers = new ArrayList<>();

    public AppointmentEventPublisher(List<AppointmentObserver> observers) {
        this.observers.addAll(observers);
    }

    public void register(AppointmentObserver observer) { observers.add(observer); }
    public void unregister(AppointmentObserver observer) { observers.remove(observer); }

    public void publish(AppointmentEvent event) {
        for (AppointmentObserver observer : observers) {
            if (!observer.supports(event.type())) continue;
            try {
                observer.onEvent(event);
            } catch (RuntimeException ex) {
                // ผู้สังเกตตัวหนึ่งล้มเหลว ต้องไม่ทำให้ธุรกรรมหลักพัง
                log.warn("observer {} ล้มเหลว: {}", observer.observerName(), ex.getMessage());
            }
        }
    }

    public List<String> registeredObservers() {
        return observers.stream().map(AppointmentObserver::observerName).toList();
    }
}
