package com.clinic.security;

import com.clinic.domain.auth.Role;
import com.clinic.exception.BusinessRuleException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** ตัวช่วยอ่านผู้ใช้ที่ล็อกอินอยู่ — ไม่ให้ controller/service ไปยุ่งกับ SecurityContext โดยตรง */
@Component
public class CurrentUser {

    public Optional<AppUserPrincipal> principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserPrincipal p)) {
            return Optional.empty();
        }
        return Optional.of(p);
    }

    public AppUserPrincipal require() {
        return principal().orElseThrow(() ->
                new BusinessRuleException("UNAUTHENTICATED", "กรุณาเข้าสู่ระบบก่อนใช้งาน"));
    }

    public String displayName() {
        return principal().map(AppUserPrincipal::getDisplayName).orElse("ระบบ");
    }

    public Role role() { return require().getRole(); }

    /** id ของผู้ป่วยที่ล็อกอินอยู่ (ใช้ในพอร์ทัลผู้ป่วย) */
    public Long requirePatientId() {
        AppUserPrincipal p = require();
        if (p.getRole() != Role.PATIENT || p.getPersonId() == null) {
            throw new BusinessRuleException("NOT_PATIENT_ACCOUNT",
                    "บัญชีนี้ไม่ใช่บัญชีผู้ป่วย");
        }
        return p.getPersonId();
    }

    /** ตรวจความเป็นเจ้าของข้อมูล: ผู้ป่วยดูได้เฉพาะของตัวเอง เจ้าหน้าที่ดูได้ทั้งหมด */
    public void assertCanAccessPatient(Long patientId) {
        AppUserPrincipal p = require();
        if (p.getRole().isInternal()) return;
        if (!patientId.equals(p.getPersonId())) {
            throw new BusinessRuleException("FORBIDDEN_PATIENT_DATA",
                    "ไม่มีสิทธิ์เข้าถึงข้อมูลของผู้ป่วยรายอื่น");
        }
    }
}
