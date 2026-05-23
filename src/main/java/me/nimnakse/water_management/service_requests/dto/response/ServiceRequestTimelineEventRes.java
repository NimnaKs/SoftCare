package me.nimnakse.water_management.service_requests.dto.response;

import java.time.Instant;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestEventType;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStageType;

public record ServiceRequestTimelineEventRes(
        Long id,
        ServiceRequestStageType stageType,
        ServiceRequestEventType eventType,
        String notes,
        String payloadJson,
        Long createdBy,
        Instant createdAt
) {}
