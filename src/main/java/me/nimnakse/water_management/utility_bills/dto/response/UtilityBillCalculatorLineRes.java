package me.nimnakse.water_management.utility_bills.dto.response;
import java.math.BigDecimal;
public record UtilityBillCalculatorLineRes(Integer fromUnit, Integer toUnit, Integer units, BigDecimal rate, BigDecimal amount) {}