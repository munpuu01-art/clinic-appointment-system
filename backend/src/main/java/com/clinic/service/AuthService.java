package com.clinic.service;

import com.clinic.dto.*;

import java.util.List;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse registerPatient(PatientRegisterRequest request);
    AccountResponse createAccount(CreateAccountRequest request);
    AccountResponse currentUser();
    void changePassword(ChangePasswordRequest request);
    List<AccountResponse> listAccounts();
    AccountResponse setActive(Long accountId, boolean active);
    AccountResponse resetPassword(Long accountId, String newPassword);
}
