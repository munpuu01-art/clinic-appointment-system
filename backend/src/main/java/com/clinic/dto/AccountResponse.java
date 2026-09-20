package com.clinic.dto;

import com.clinic.domain.auth.Role;

import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String username,
        String displayName,
        Role role,
        String roleLabel,
        Long personId,
        boolean active,
        boolean locked,
        LocalDateTime lastLoginAt
) { }
