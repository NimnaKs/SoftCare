package me.nimnakse.water_management.service_requests.dto.response;

import java.util.List;

public record ServiceRequestDashboardRes(
        long openCount,
        long inProgressCount,
        long pausedCount,
        long closedCount,
        long expiredCount,
        List<ServiceRequestListRes> expiredRequests
) {}
