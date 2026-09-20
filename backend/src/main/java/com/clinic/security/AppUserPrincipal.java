package com.clinic.security;

import com.clinic.domain.auth.Role;
import com.clinic.domain.auth.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapter Pattern — แปลง UserAccount (Domain ของเรา) ให้เป็น UserDetails ที่ Spring Security รู้จัก
 * ทำให้ Domain ไม่ต้องขึ้นต่อ framework
 */
public class AppUserPrincipal implements UserDetails {

    private final Long accountId;
    private final String username;
    private final String passwordHash;
    private final Role role;
    private final Long personId;
    private final String displayName;
    private final boolean active;
    private final boolean locked;

    public AppUserPrincipal(UserAccount account) {
        this.accountId = account.getId();
        this.username = account.getUsername();
        this.passwordHash = account.getPasswordHash();
        this.role = account.getRole();
        this.personId = account.linkedPersonId();
        this.displayName = account.resolveDisplayName();
        this.active = account.isActive();
        this.locked = account.isLocked();
    }

    public Long getAccountId() { return accountId; }
    public Role getRole() { return role; }
    /** id ของ Person ที่ผูกอยู่ — ใช้ตรวจความเป็นเจ้าของข้อมูลในพอร์ทัลผู้ป่วย */
    public Long getPersonId() { return personId; }
    public String getDisplayName() { return displayName; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.authority()));
    }

    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return !locked; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return active; }
}
