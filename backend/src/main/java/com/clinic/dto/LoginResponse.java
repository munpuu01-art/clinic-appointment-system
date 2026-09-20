package com.clinic.dto;

import com.clinic.domain.auth.Role;

import java.util.Set;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        Long accountId,
        String username,
        String displayName,
        Role role,
        String roleLabel,
        Long personId,
        String hn,
        Set<String> permissions,
        boolean mustChangePassword
) { }
