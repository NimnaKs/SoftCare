package me.nimnakse.water_management.service_requests.dto.request;

import jakarta.validation.constraints.NotNull;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestFinalResponse;

public record ServiceRequestFeedbackReq(
        @NotNull ServiceRequestFinalResponse finalResponse,
        String remarks
) {}
