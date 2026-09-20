package com.clinic.exception;

/** ช่วงเวลาถูกจองไปแล้ว */
public class DoubleBookingException extends BusinessRuleException {
    public DoubleBookingException(String message) {
        super("DOUBLE_BOOKING", message);
    }
}
