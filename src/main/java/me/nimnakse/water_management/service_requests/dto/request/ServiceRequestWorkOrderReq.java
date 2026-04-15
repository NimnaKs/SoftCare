package me.nimnakse.water_management.service_requests.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestWorkOrderAction;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestWorkOrderStatus;

public record ServiceRequestWorkOrderReq(
        @NotNull ServiceRequestWorkOrderAction actionType,
        LocalDate committeeMeetingDate,
        String notes,
        List<Long> employeeIds,
        ServiceRequestWorkOrderStatus status
) {}
