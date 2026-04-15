package me.nimnakse.water_management.service_requests.dto.response;

import java.time.Instant;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestFinalResponse;

public record ServiceRequestFeedbackRes(
        Long id,
        Long serviceRequestId,
        ServiceRequestFinalResponse finalResponse,
        Instant updatedAt
) {}
