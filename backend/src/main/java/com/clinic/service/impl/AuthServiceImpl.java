package com.clinic.service.impl;

import com.clinic.domain.auth.Role;
import com.clinic.domain.auth.UserAccount;
import com.clinic.domain.person.Person;
import com.clinic.domain.person.Patient;
import com.clinic.dto.*;
import com.clinic.exception.BusinessRuleException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.repository.StaffRepository;
import com.clinic.repository.UserAccountRepository;
import com.clinic.security.AppUserPrincipal;
import com.clinic.security.CurrentUser;
import com.clinic.security.JwtTokenService;
import com.clinic.service.AuthService;
import com.clinic.service.PatientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ตรรกะการยืนยันตัวตนทั้งหมด
 * สังเกตว่า service ไม่ได้ตรวจรหัสผ่านเอง แต่มอบให้ PasswordEncoder (Dependency Inversion)
 * และไม่ได้นับครั้งที่ผิดเอง แต่ให้ UserAccount จัดการ (Encapsulation)
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository accounts;
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final StaffRepository staffs;
    private final PatientService patientService;
    private final PasswordEncoder encoder;
    private final JwtTokenService tokenService;
    private final CurrentUser currentUser;

    public AuthServiceImpl(UserAccountRepository accounts, PatientRepository patients,
                           DoctorRepository doctors, StaffRepository staffs,
                           PatientService patientService, PasswordEncoder encoder,
                           JwtTokenService tokenService, CurrentUser currentUser) {
        this.accounts = accounts;
        this.patients = patients;
        this.doctors = doctors;
        this.staffs = staffs;
        this.patientService = patientService;
        this.encoder = encoder;
        this.tokenService = tokenService;
        this.currentUser = currentUser;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        UserAccount account = accounts.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new BusinessRuleException("BAD_CREDENTIALS",
                        "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง"));

        if (!account.isActive()) {
            throw new BusinessRuleException("ACCOUNT_DISABLED", "บัญชีนี้ถูกระงับการใช้งาน");
        }
        if (account.isLocked()) {
            throw new BusinessRuleException("ACCOUNT_LOCKED",
                    "บัญชีถูกล็อกชั่วคราวจากการกรอกรหัสผ่านผิดหลายครั้ง กรุณารอ 15 นาที");
        }
        if (!encoder.matches(request.password(), account.getPasswordHash())) {
            account.recordFailedLogin();
            accounts.save(account);
            throw new BusinessRuleException("BAD_CREDENTIALS",
                    "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
        }

        account.recordSuccessfulLogin();
        accounts.save(account);
        return toLoginResponse(account);
    }

    @Override
    public LoginResponse registerPatient(PatientRegisterRequest request) {
        if (accounts.existsByUsernameIgnoreCase(request.username())) {
            throw new BusinessRuleException("USERNAME_TAKEN", "ชื่อผู้ใช้นี้ถูกใช้งานแล้ว");
        }
        PatientResponse created = patientService.register(request.patient());
        Patient patient = patientService.getEntity(created.id());

        UserAccount account = new UserAccount(request.username(),
                encoder.encode(request.password()), Role.PATIENT, patient, patient.getDisplayName());
        accounts.save(account);
        return toLoginResponse(account);
    }

    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (accounts.existsByUsernameIgnoreCase(request.username())) {
            throw new BusinessRuleException("USERNAME_TAKEN", "ชื่อผู้ใช้นี้ถูกใช้งานแล้ว");
        }
        Person person = resolvePerson(request.role(), request.personId());
        if (person != null && accounts.existsByPersonId(person.getId())) {
            throw new BusinessRuleException("PERSON_HAS_ACCOUNT", "บุคคลนี้มีบัญชีผู้ใช้อยู่แล้ว");
        }
        UserAccount account = new UserAccount(request.username(),
                encoder.encode(request.password()), request.role(), person,
                request.displayName());
        account.setMustChangePassword(true);
        return toAccountResponse(accounts.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse currentUser() {
        return toAccountResponse(loadCurrentAccount());
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        UserAccount account = loadCurrentAccount();
        if (!encoder.matches(request.currentPassword(), account.getPasswordHash())) {
            throw new BusinessRuleException("BAD_CREDENTIALS", "รหัสผ่านเดิมไม่ถูกต้อง");
        }
        if (request.currentPassword().equals(request.newPassword())) {
            throw new BusinessRuleException("SAME_PASSWORD", "รหัสผ่านใหม่ต้องไม่ซ้ำกับรหัสผ่านเดิม");
        }
        account.changePassword(encoder.encode(request.newPassword()));
        accounts.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> listAccounts() {
        return accounts.findAll().stream().map(this::toAccountResponse).toList();
    }

    @Override
    public AccountResponse setActive(Long accountId, boolean active) {
        UserAccount account = accounts.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบบัญชีผู้ใช้ id=" + accountId));
        if (active) account.activate(); else account.deactivate();
        return toAccountResponse(accounts.save(account));
    }

    @Override
    public AccountResponse resetPassword(Long accountId, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new BusinessRuleException("WEAK_PASSWORD", "รหัสผ่านต้องยาวอย่างน้อย 8 ตัวอักษร");
        }
        UserAccount account = accounts.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบบัญชีผู้ใช้ id=" + accountId));
        account.changePassword(encoder.encode(newPassword));
        account.setMustChangePassword(true);
        return toAccountResponse(accounts.save(account));
    }

    /** ---------- helper ---------- */

    private UserAccount loadCurrentAccount() {
        AppUserPrincipal principal = currentUser.require();
        return accounts.findById(principal.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบบัญชีผู้ใช้"));
    }

    private Person resolvePerson(Role role, Long personId) {
        if (personId == null) {
            if (role == Role.ADMIN) return null;   // admin ไม่จำเป็นต้องผูกกับบุคคล
            throw new BusinessRuleException("PERSON_REQUIRED",
                    "บทบาท " + role.getLabel() + " ต้องระบุบุคคลที่ผูกกับบัญชี");
        }
        return switch (role) {
            case PATIENT -> patients.findById(personId)
                    .orElseThrow(() -> new ResourceNotFoundException("ไม่พบผู้ป่วย id=" + personId));
            case DOCTOR -> doctors.findById(personId)
                    .orElseThrow(() -> new ResourceNotFoundException("ไม่พบแพทย์ id=" + personId));
            case STAFF -> staffs.findById(personId)
                    .orElseThrow(() -> new ResourceNotFoundException("ไม่พบเจ้าหน้าที่ id=" + personId));
            case ADMIN -> null;
        };
    }

    private LoginResponse toLoginResponse(UserAccount account) {
        AppUserPrincipal principal = new AppUserPrincipal(account);
        String hn = (account.getPerson() instanceof Patient p) ? p.getHn() : null;
        return new LoginResponse(
                tokenService.issue(principal),
                "Bearer",
                tokenService.expiresInSeconds(),
                account.getId(),
                account.getUsername(),
                account.resolveDisplayName(),
                account.getRole(),
                account.getRole().getLabel(),
                account.linkedPersonId(),
                hn,
                account.getRole().getPermissions(),
                account.isMustChangePassword());
    }

    private AccountResponse toAccountResponse(UserAccount account) {
        return new AccountResponse(
                account.getId(),
                account.getUsername(),
                account.resolveDisplayName(),
                account.getRole(),
                account.getRole().getLabel(),
                account.linkedPersonId(),
                account.isActive(),
                account.isLocked(),
                account.getLastLoginAt());
    }
}
