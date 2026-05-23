package me.nimnakse.water_management.service_requests.dto.request;

import jakarta.validation.constraints.NotNull;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestResolutionType;

public record ServiceRequestSolutionReq(
        @NotNull ServiceRequestResolutionType resolutionType,
        String serialNumber,
        Integer meterReading,
        String adjustmentDescription,
        String description
) {}
