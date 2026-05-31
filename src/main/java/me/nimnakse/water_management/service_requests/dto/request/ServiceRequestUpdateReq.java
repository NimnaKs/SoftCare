package me.nimnakse.water_management.service_requests.dto.request;

import me.nimnakse.water_management.service_requests.entity.ServiceRequestCategory;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestGroup;

public record ServiceRequestUpdateReq(
        Long connectionId,
        String accountNumber,
        ServiceRequestGroup requestGroup,
        ServiceRequestCategory category,
        String description
) {}
