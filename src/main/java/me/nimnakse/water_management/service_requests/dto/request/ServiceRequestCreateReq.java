package me.nimnakse.water_management.service_requests.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestCategory;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestGroup;

public record ServiceRequestCreateReq(
        Long connectionId,
        String accountNumber,
        @NotNull ServiceRequestGroup requestGroup,
        @NotNull ServiceRequestCategory category,
        @NotBlank String description,
        String contactMobileNumber,
        Boolean saveAsDraft
) {}
