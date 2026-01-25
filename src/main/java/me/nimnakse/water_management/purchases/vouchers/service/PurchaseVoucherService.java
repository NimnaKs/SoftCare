package me.nimnakse.water_management.purchases.vouchers.service;

import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.purchases.vouchers.dto.response.PurchaseVoucherRes;

public interface PurchaseVoucherService {
    PurchaseVoucherRes getById(Long id);

    PageResponse<PurchaseVoucherRes> getPage(int page, int size);
}
