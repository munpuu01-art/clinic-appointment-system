package com.clinic.service;

import com.clinic.dto.DashboardResponse;

import java.time.LocalDate;

public interface DashboardService {
    DashboardResponse summary(LocalDate date);
}
