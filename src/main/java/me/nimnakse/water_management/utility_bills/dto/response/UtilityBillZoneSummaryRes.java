package me.nimnakse.water_management.utility_bills.dto.response;
import java.time.LocalDate;
public record UtilityBillZoneSummaryRes(Long billingZoneId, String billingZoneName, LocalDate lastGeneratedDate, Long daysSinceLastGenerated, long pendingCount, long activeCount, long disconnectedCount, String lateFeeMethod, String redBillMethod) {}