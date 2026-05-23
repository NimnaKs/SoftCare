package me.nimnakse.water_management.service_requests.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestBillStatus;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestMeterAction;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestResolutionType;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestSolutionStatus;

public record ServiceRequestSolutionRes(
        Long id,
        Long serviceRequestId,
        ServiceRequestResolutionType resolutionType,
        String description,
        String serialNumber,
        Integer meterReading,
        String adjustmentDescription,
        String beforeConnectionStatus,
        String afterConnectionStatus,
        String beforeMeterStatus,
        String afterMeterStatus,
        ServiceRequestMeterAction meterAction,
        ServiceRequestSolutionStatus status,
        ServiceRequestBillStatus billStatusAtResolution,
        Boolean requiresReconnectionFee,
        BigDecimal reconnectionFeeAmount,
        Long tariffId,
        Long invoiceId,
        Instant appliedAt,
        Long appliedBy,
        Instant createdAt,
        Instant updatedAt
) {}
