package com.clinic.exception;

/** ไม่พบข้อมูลที่ร้องขอ (HTTP 404) */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Object id) {
        super("ไม่พบ " + resource + " รหัส " + id);
    }
    public ResourceNotFoundException(String message) { super(message); }
}
