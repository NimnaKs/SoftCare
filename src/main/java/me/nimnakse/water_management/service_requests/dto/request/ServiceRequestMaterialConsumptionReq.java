package me.nimnakse.water_management.service_requests.dto.request;

import java.math.BigDecimal;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestMaterialConsumptionStatus;

public record ServiceRequestMaterialConsumptionReq(
        String description,
        BigDecimal maintainCharge,
        ServiceRequestMaterialConsumptionStatus status,
        Boolean createInvoice
) {}
