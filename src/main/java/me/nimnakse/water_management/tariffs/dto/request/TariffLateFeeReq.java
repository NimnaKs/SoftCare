package me.nimnakse.water_management.tariffs.dto.request;

public record TariffLateFeeReq(
                boolean applyNormalBill,
                boolean applyRedBill,
                String chargingMethod,
                String name,
                Integer overduePeriod,
                Double limitExceeded,
                Double fixedAmount,
                Double percentage) {
}
