package me.nimnakse.water_management.purchases.service;

import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.purchases.dto.response.GrnInvoiceRes;

public interface GrnInvoiceService {
    GrnInvoiceRes getById(Long id);

    PageResponse<GrnInvoiceRes> getPage(int page, int size);
}
