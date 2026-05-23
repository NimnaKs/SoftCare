package me.nimnakse.water_management.service_requests.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestMaterialConsumptionStatus;

public record ServiceRequestMaterialConsumptionRes(
        Long id,
        Long serviceRequestId,
        String description,
        BigDecimal maintainCharge,
        Long invoiceId,
        ServiceRequestMaterialConsumptionStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
