package me.nimnakse.water_management.utility_bills.dto.request;
public record UtilityBillCalculatorReq(Long tariffId, Integer previousReading, Integer currentReading) {}