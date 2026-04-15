package me.nimnakse.water_management.utility_bills.dto.request;
import java.time.LocalDate;
public record UtilityBillMeterReadReq(Integer currentReading, LocalDate meterReadDate, String note) {}