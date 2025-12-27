package me.nimnakse.water_management.revenue.accounts.service;

import java.util.List;
import me.nimnakse.water_management.revenue.accounts.dto.request.RevenueAccountCreateReq;
import me.nimnakse.water_management.revenue.accounts.dto.request.RevenueAccountUpdateReq;
import me.nimnakse.water_management.revenue.accounts.dto.response.RevenueAccountRes;

public interface RevenueAccountService {
    RevenueAccountRes create(RevenueAccountCreateReq request);

    RevenueAccountRes update(Long id, RevenueAccountUpdateReq request);

    RevenueAccountRes getById(Long id);

    List<RevenueAccountRes> list(Long mainCategoryId);

    void delete(Long id);
}
