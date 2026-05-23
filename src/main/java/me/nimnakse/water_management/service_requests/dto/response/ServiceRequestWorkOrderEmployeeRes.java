package me.nimnakse.water_management.service_requests.dto.response;

import java.time.Instant;

public record ServiceRequestWorkOrderEmployeeRes(
        Long id,
        Long employeeId,
        String employeeName,
        Instant assignedAt
) {}
