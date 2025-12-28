package me.nimnakse.water_management.liabilities.main_categories.service;

import java.util.List;
import me.nimnakse.water_management.liabilities.main_categories.dto.request.LiabilityMainCategoryCreateReq;
import me.nimnakse.water_management.liabilities.main_categories.dto.request.LiabilityMainCategoryUpdateReq;
import me.nimnakse.water_management.liabilities.main_categories.dto.response.LiabilityMainCategoryRes;

public interface LiabilityMainCategoryService {
    LiabilityMainCategoryRes create(LiabilityMainCategoryCreateReq request);

    LiabilityMainCategoryRes update(Long id, LiabilityMainCategoryUpdateReq request);

    LiabilityMainCategoryRes getById(Long id);

    List<LiabilityMainCategoryRes> list();

    void delete(Long id);
}
