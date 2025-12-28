package me.nimnakse.water_management.liabilities.accounts.service;

import java.util.List;
import me.nimnakse.water_management.liabilities.accounts.dto.request.LiabilityAccountCreateReq;
import me.nimnakse.water_management.liabilities.accounts.dto.request.LiabilityAccountUpdateReq;
import me.nimnakse.water_management.liabilities.accounts.dto.response.LiabilityAccountRes;

public interface LiabilityAccountService {
    LiabilityAccountRes create(LiabilityAccountCreateReq request);

    LiabilityAccountRes update(Long id, LiabilityAccountUpdateReq request);

    LiabilityAccountRes getById(Long id);

    List<LiabilityAccountRes> list(Long mainCategoryId);

    void delete(Long id);
}
