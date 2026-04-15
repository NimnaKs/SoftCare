package me.nimnakse.water_management.service_requests.dto.request;

import java.math.BigDecimal;

public record ServiceRequestMaterialConsumptionReq(
        Boolean hasMcnForm,
        String mcnReference,
        Boolean importFromMcn,
        BigDecimal maintainChargeAmount,
        String description
) {}
