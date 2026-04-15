package me.nimnakse.water_management.service_requests.dto.response;

import java.time.Instant;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestCategory;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestGroup;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStatus;

public record ServiceRequestListRes(
        Long id,
        Long orgUnitId,
        Long connectionId,
        String accountNumber,
        ServiceRequestGroup requestGroup,
        ServiceRequestCategory category,
        String description,
        ServiceRequestStatus status,
        Instant savedAt,
        Instant closedAt,
        Integer expiryDays,
        String contactMobileNumber,
        Long elapsedMinutes,
        String elapsedDisplay,
        boolean expired
) {}
