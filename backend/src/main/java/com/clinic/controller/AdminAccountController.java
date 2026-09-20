package com.clinic.controller;

import com.clinic.dto.AccountResponse;
import com.clinic.dto.CreateAccountRequest;
import com.clinic.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** จัดการบัญชีผู้ใช้ — เฉพาะผู้ดูแลระบบ */
@RestController
@RequestMapping("/api/admin/accounts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountController {

    private final AuthService authService;

    public AdminAccountController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public List<AccountResponse> list() { return authService.listAccounts(); }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createAccount(request));
    }

    @PatchMapping("/{id}/activate")
    public AccountResponse activate(@PathVariable Long id) {
        return authService.setActive(id, true);
    }

    @PatchMapping("/{id}/deactivate")
    public AccountResponse deactivate(@PathVariable Long id) {
        return authService.setActive(id, false);
    }

    @PatchMapping("/{id}/reset-password")
    public AccountResponse resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return authService.resetPassword(id, body.get("newPassword"));
    }
}
