package me.nimnakse.water_management.service_requests.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestResolutionType;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestSolutionStatus;

public record ServiceRequestSolutionRes(
        Long id,
        Long serviceRequestId,
        ServiceRequestResolutionType resolutionType,
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
        Boolean billIsOpen,
        Boolean pendingAccountUpdate,
        BigDecimal reconnectionFee,
        ServiceRequestSolutionStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
