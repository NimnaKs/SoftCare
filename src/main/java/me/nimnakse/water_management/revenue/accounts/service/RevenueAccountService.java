package me.nimnakse.water_management.revenue.accounts.service;

import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.revenue.accounts.dto.request.RevenueAccountCreateReq;
import me.nimnakse.water_management.revenue.accounts.dto.request.RevenueAccountUpdateReq;
import me.nimnakse.water_management.revenue.accounts.dto.response.RevenueAccountRes;

public interface RevenueAccountService {
    RevenueAccountRes create(RevenueAccountCreateReq request);

    RevenueAccountRes update(Long id, RevenueAccountUpdateReq request);

    RevenueAccountRes getById(Long id);

    PageResponse<RevenueAccountRes> list(Long mainCategoryId, int page, int size);

    void delete(Long id);
}
