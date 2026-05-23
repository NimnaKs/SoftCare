package me.nimnakse.water_management.service_requests.dto.response;

import java.util.List;

public record ServiceRequestDashboardRes(
        long draftCount,
        long submittedCount,
        long inProgressCount,
        long pausedCount,
        long resolvedCount,
        long closedCount,
        long expiredCount,
        List<ServiceRequestListRes> expiredRequests
) {}
