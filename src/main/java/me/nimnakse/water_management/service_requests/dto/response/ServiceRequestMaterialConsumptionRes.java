package me.nimnakse.water_management.service_requests.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ServiceRequestMaterialConsumptionRes(
        Long id,
        Long serviceRequestId,
        Boolean hasMcnForm,
        String mcnReference,
        Boolean importFromMcn,
        BigDecimal maintainChargeAmount,
        String description,
        Instant createdAt
) {}
