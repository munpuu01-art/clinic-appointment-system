package com.clinic.rules;

/**
 * Chain of Responsibility: กฎการจอง 1 ข้อ
 * แต่ละกฎตรวจเรื่องเดียว (Single Responsibility) แล้วส่งต่อให้กฎถัดไป
 */
public interface BookingRule {
    int order();
    String ruleName();
    void check(BookingContext context);
    void setNext(BookingRule next);
    void validate(BookingContext context);
}
