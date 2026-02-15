package me.nimnakse.water_management.sales.invoices.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.nimnakse.water_management.connections.entity.ConnectionStatus;
import me.nimnakse.water_management.sales.invoices.dto.ConnectionIncludeGroupType;
import me.nimnakse.water_management.sales.invoices.dto.ConnectionIncludeMode;

public record SalesInvoiceConnectionPreviewReq(
        Long orgUnitId,
        @NotNull(message = "includeMode is required") ConnectionIncludeMode includeMode,
        List<String> includeAccountNumbers,
        ConnectionStatus includeStatus,
        ConnectionIncludeGroupType includeGroupType,
        List<Long> includeGroupIds,
        List<String> excludeAccountNumbers,
        Boolean excludeAllDisconnected,
        Boolean excludeAllPending
) {
}
