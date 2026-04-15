package me.nimnakse.water_management.service_requests.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestResolutionType;

public record ServiceRequestSolutionReq(
        @NotNull ServiceRequestResolutionType resolutionType,
        String beforeConnectionStatus,
        String afterConnectionStatus,
        String beforeMeterStatus,
        String afterMeterStatus,
        String meterStatus,
        String systemAction,
        String serialNumber,
        Integer meterReading,
        String adjustmentDescription,
        String otherDescription,
        String description,
        BigDecimal reconnectionFee
) {}
