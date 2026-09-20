package com.clinic.domain.auth;

import com.clinic.domain.common.BaseEntity;
import com.clinic.domain.person.Person;
import com.clinic.exception.BusinessRuleException;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * บัญชีผู้ใช้งาน — ผูกกับ Person (ผู้ป่วย/แพทย์/เจ้าหน้าที่) ได้แบบ optional
 * ผู้ดูแลระบบอาจไม่มี Person ผูกอยู่ก็ได้
 *
 * Encapsulation: การล็อกบัญชี การนับครั้งที่ล็อกอินผิด และการเปลี่ยนรหัสผ่าน
 * ถูกควบคุมด้วยเมธอดของคลาสนี้เท่านั้น
 */
@Entity
@Table(name = "user_account")
public class UserAccount extends BaseEntity {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** บุคคลที่บัญชีนี้เป็นตัวแทน (ผู้ป่วย/แพทย์/เจ้าหน้าที่) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword = false;

    protected UserAccount() { }

    public UserAccount(String username, String passwordHash, Role role, Person person, String displayName) {
        if (username == null || username.isBlank()) {
            throw new BusinessRuleException("USERNAME_REQUIRED", "กรุณาระบุชื่อผู้ใช้");
        }
        this.username = username.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.role = role;
        this.person = person;
        this.displayName = displayName;
    }

    /** ---------- พฤติกรรมทางธุรกิจ ---------- */

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public void recordFailedLogin() {
        this.failedAttempts++;
        if (this.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(15);
            this.failedAttempts = 0;
        }
    }

    public void recordSuccessfulLogin() {
        this.failedAttempts = 0;
        this.lockedUntil = null;
        this.lastLoginAt = LocalDateTime.now();
    }

    public void changePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new BusinessRuleException("PASSWORD_REQUIRED", "รหัสผ่านใหม่ไม่ถูกต้อง");
        }
        this.passwordHash = newPasswordHash;
        this.mustChangePassword = false;
    }

    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; this.lockedUntil = null; this.failedAttempts = 0; }

    public boolean can(String permission) { return role != null && role.can(permission); }

    /** id ของผู้ป่วยที่บัญชีนี้เป็นเจ้าของ (null ถ้าไม่ใช่บัญชีผู้ป่วย) */
    public Long linkedPersonId() { return person == null ? null : person.getId(); }

    public String resolveDisplayName() {
        if (displayName != null && !displayName.isBlank()) return displayName;
        return person != null ? person.getDisplayName() : username;
    }

    /** ---------- getters ---------- */
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String v) { this.displayName = v; }
    public boolean isActive() { return active; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean v) { this.mustChangePassword = v; }
}
