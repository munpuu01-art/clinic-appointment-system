package com.clinic.domain.auth;

import java.util.Set;

/**
 * บทบาทผู้ใช้ในระบบ — แต่ละบทบาทรู้ "สิทธิ์" ของตัวเอง
 * (Encapsulation: ตรรกะสิทธิ์อยู่กับ enum ไม่กระจายไปตาม if ในหลายที่)
 */
public enum Role {

    ADMIN("ผู้ดูแลระบบ", Set.of("MANAGE_USERS", "MANAGE_DOCTORS", "MANAGE_APPOINTMENTS",
            "MANAGE_QUEUE", "MANAGE_BILLING", "VIEW_DASHBOARD", "VIEW_ALL_PATIENTS")),

    STAFF("เจ้าหน้าที่เวชระเบียน", Set.of("MANAGE_APPOINTMENTS", "MANAGE_QUEUE",
            "MANAGE_BILLING", "VIEW_DASHBOARD", "VIEW_ALL_PATIENTS")),

    DOCTOR("แพทย์", Set.of("MANAGE_QUEUE", "MANAGE_MEDICAL_RECORD", "VIEW_DASHBOARD",
            "VIEW_ALL_PATIENTS")),

    PATIENT("ผู้ป่วย", Set.of("VIEW_OWN_APPOINTMENTS", "BOOK_OWN_APPOINTMENT",
            "CANCEL_OWN_APPOINTMENT", "VIEW_OWN_RECORDS"));

    private final String label;
    private final Set<String> permissions;

    Role(String label, Set<String> permissions) {
        this.label = label;
        this.permissions = permissions;
    }

    public String getLabel() { return label; }
    public Set<String> getPermissions() { return permissions; }

    public boolean can(String permission) { return permissions.contains(permission); }

    /** บทบาทฝั่งเจ้าหน้าที่คลินิก (ใช้หน้าจอหลังบ้าน) */
    public boolean isInternal() { return this != PATIENT; }

    /** ชื่อ authority ที่ Spring Security ใช้ */
    public String authority() { return "ROLE_" + name(); }
}
