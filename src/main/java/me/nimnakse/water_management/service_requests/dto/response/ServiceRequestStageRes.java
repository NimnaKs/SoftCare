package me.nimnakse.water_management.service_requests.dto.response;

import java.time.Instant;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStageStatus;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStageType;

public record ServiceRequestStageRes(
        Long id,
        ServiceRequestStageType stageType,
        ServiceRequestStageStatus status,
        Instant startedAt,
        Instant pausedAt,
        Instant completedAt
) {}
