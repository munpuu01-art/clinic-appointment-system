package com.clinic.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/** Value Object: ข้อมูลติดต่อ ใช้ร่วมกันทั้ง Patient / Doctor / Staff */
@Embeddable
public class ContactInfo {

    @Pattern(regexp = "^0\\d{8,9}$", message = "เบอร์โทรต้องขึ้นต้นด้วย 0 และมี 9-10 หลัก")
    @Column(name = "phone", length = 10)
    private String phone;

    @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "line_id", length = 60)
    private String lineId;

    protected ContactInfo() { }

    public ContactInfo(String phone, String email, String lineId) {
        this.phone = phone;
        this.email = email;
        this.lineId = lineId;
    }

    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getLineId() { return lineId; }

    public boolean hasEmail() { return email != null && !email.isBlank(); }
    public boolean hasPhone() { return phone != null && !phone.isBlank(); }

    /** ปิดบังเบอร์โทรสำหรับแสดงบนจอคิวสาธารณะ */
    public String maskedPhone() {
        if (!hasPhone()) return "-";
        return phone.substring(0, 3) + "-XXX-" + phone.substring(phone.length() - 3);
    }
}
