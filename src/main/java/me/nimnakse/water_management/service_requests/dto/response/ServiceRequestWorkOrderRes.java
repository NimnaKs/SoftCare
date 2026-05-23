package me.nimnakse.water_management.service_requests.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestWorkOrderAction;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestWorkOrderStatus;

public record ServiceRequestWorkOrderRes(
        Long id,
        Long serviceRequestId,
        ServiceRequestWorkOrderAction actionType,
        LocalDate committeeMeetingDate,
        String notes,
        ServiceRequestWorkOrderStatus status,
        List<ServiceRequestWorkOrderEmployeeRes> employees,
        Long createdBy,
        Long updatedBy,
        Instant createdAt,
        Instant updatedAt
) {}
