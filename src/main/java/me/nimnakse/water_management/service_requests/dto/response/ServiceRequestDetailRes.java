package me.nimnakse.water_management.service_requests.dto.response;

import java.time.Instant;
import java.util.List;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestCategory;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestGroup;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStageType;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStatus;

public record ServiceRequestDetailRes(
        Long id,
        String ticketNo,
        Long orgUnitId,
        Long connectionId,
        String accountNumber,
        Long connectionTariffId,
        ServiceRequestGroup requestGroup,
        ServiceRequestCategory category,
        String customerNameSnapshot,
        String description,
        ServiceRequestStatus status,
        ServiceRequestStageType currentStage,
        Instant savedAt,
        Instant submittedAt,
        Instant closedAt,
        Instant expiryAt,
        Long totalPausedMinutes,
        String contactMobileNumber,
        Long elapsedMinutes,
        String elapsedDisplay,
        boolean expired,
        List<ServiceRequestStageRes> stages,
        List<ServiceRequestWorkOrderRes> workOrders,
        List<ServiceRequestSolutionRes> solutions,
        ServiceRequestMaterialConsumptionRes materialConsumption,
        ServiceRequestFeedbackRes feedback,
        List<ServiceRequestTimelineEventRes> timeline
) {}
