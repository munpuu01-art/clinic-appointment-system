package com.clinic.rules;

/** ฐานของกฎทุกข้อ — จัดการการส่งต่อไปยังกฎถัดไปในโซ่ */
public abstract class AbstractBookingRule implements BookingRule {

    private BookingRule next;

    @Override
    public void setNext(BookingRule next) { this.next = next; }

    @Override
    public final void validate(BookingContext context) {
        check(context);                       // ถ้าผิดกฎจะโยน BusinessRuleException ออกไป
        if (next != null) next.validate(context);
    }

    @Override
    public String ruleName() { return getClass().getSimpleName(); }
}
