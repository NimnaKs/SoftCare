package me.nimnakse.water_management.tariffs.dto.response;

public record TariffLateFeeRes(
                Long id,
                boolean applyNormalBill,
                boolean applyRedBill,
                String chargingMethod,
                String name,
                Integer overduePeriod,
                Double limitExceeded,
                Double fixedAmount,
                Double percentage) {
}
