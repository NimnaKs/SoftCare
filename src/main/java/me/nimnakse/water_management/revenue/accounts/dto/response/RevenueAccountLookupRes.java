package me.nimnakse.water_management.revenue.accounts.dto.response;

public record RevenueAccountLookupRes(
        Long id,
        String accountNumber,
        String name,
        String referencePrefix,
        String mainCategoryName
) {
}
