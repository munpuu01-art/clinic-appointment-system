package com.clinic.exception;

/** ฐานของข้อผิดพลาดเชิงกฎธุรกิจทั้งหมด (HTTP 409/422) */
public class BusinessRuleException extends RuntimeException {
    private final String code;

    public BusinessRuleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
}
