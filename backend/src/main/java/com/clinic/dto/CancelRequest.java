package com.clinic.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelRequest(
        @NotBlank(message = "กรุณาระบุเหตุผลการยกเลิก") String reason,
        String cancelledBy
) { }
