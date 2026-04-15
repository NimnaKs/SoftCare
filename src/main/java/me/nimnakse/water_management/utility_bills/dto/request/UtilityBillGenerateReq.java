package me.nimnakse.water_management.utility_bills.dto.request;
import java.time.LocalDate; import java.util.List;
public record UtilityBillGenerateReq(Long orgUnitId, List<Long> billingZoneIds, String operationMonth, LocalDate billingDate, String password, List<Long> stopRecurringInvoiceIds, String customNote) {}