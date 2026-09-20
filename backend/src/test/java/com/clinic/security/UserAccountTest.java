package com.clinic.security;

import com.clinic.domain.auth.Role;
import com.clinic.domain.auth.UserAccount;
import com.clinic.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** TC-18..TC-21 : ทดสอบบัญชีผู้ใช้และสิทธิ์ตามบทบาท */
class UserAccountTest {

    private UserAccount account(Role role) {
        return new UserAccount("tester", "hashed-password", role, null, "ผู้ทดสอบ");
    }

    @Test
    @DisplayName("TC-18 กรอกรหัสผ่านผิดครบ 5 ครั้ง บัญชีต้องถูกล็อกชั่วคราว")
    void locksAfterFiveFailedAttempts() {
        UserAccount user = account(Role.STAFF);

        for (int i = 0; i < 4; i++) {
            user.recordFailedLogin();
            assertThat(user.isLocked()).isFalse();
        }

        user.recordFailedLogin();   // ครั้งที่ 5
        assertThat(user.isLocked()).isTrue();
    }

    @Test
    @DisplayName("TC-19 ล็อกอินสำเร็จต้องล้างจำนวนครั้งที่ผิดและปลดล็อก")
    void successfulLoginResetsCounters() {
        UserAccount user = account(Role.PATIENT);
        for (int i = 0; i < 5; i++) user.recordFailedLogin();
        assertThat(user.isLocked()).isTrue();

        user.recordSuccessfulLogin();

        assertThat(user.isLocked()).isFalse();
        assertThat(user.getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("TC-20 บทบาทผู้ป่วยต้องไม่มีสิทธิ์ของเจ้าหน้าที่")
    void patientRoleHasOnlySelfServicePermissions() {
        assertThat(Role.PATIENT.can("BOOK_OWN_APPOINTMENT")).isTrue();
        assertThat(Role.PATIENT.can("VIEW_ALL_PATIENTS")).isFalse();
        assertThat(Role.PATIENT.can("MANAGE_USERS")).isFalse();
        assertThat(Role.PATIENT.isInternal()).isFalse();

        assertThat(Role.ADMIN.can("MANAGE_USERS")).isTrue();
        assertThat(Role.STAFF.can("MANAGE_USERS")).isFalse();
        assertThat(Role.DOCTOR.isInternal()).isTrue();
    }

    @Test
    @DisplayName("TC-21 สร้างบัญชีโดยไม่มีชื่อผู้ใช้ไม่ได้ และชื่อผู้ใช้ต้องเก็บเป็นตัวพิมพ์เล็ก")
    void usernameIsRequiredAndNormalised() {
        assertThatThrownBy(() -> new UserAccount("  ", "hash", Role.ADMIN, null, null))
                .isInstanceOf(BusinessRuleException.class);

        UserAccount user = new UserAccount("  AdMiN ", "hash", Role.ADMIN, null, "ผู้ดูแลระบบ");
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.resolveDisplayName()).isEqualTo("ผู้ดูแลระบบ");
    }
}
