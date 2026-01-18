package me.nimnakse.water_management.cash_accounts.service;

import me.nimnakse.water_management.cash_accounts.dto.request.CashAccountCreateReq;
import me.nimnakse.water_management.cash_accounts.dto.request.CashAccountUpdateReq;
import me.nimnakse.water_management.cash_accounts.dto.response.CashAccountRes;
import me.nimnakse.water_management.common.api.PageResponse;

public interface CashAccountService {
    CashAccountRes create(CashAccountCreateReq request);

    CashAccountRes update(Long id, CashAccountUpdateReq request);

    CashAccountRes getById(Long id);

    PageResponse<CashAccountRes> getPage(int page, int size);

    void deactivate(Long id);
}
