package me.nimnakse.water_management.revenue.main_categories.service;

import java.util.List;
import me.nimnakse.water_management.revenue.main_categories.dto.request.RevenueMainCategoryCreateReq;
import me.nimnakse.water_management.revenue.main_categories.dto.request.RevenueMainCategoryUpdateReq;
import me.nimnakse.water_management.revenue.main_categories.dto.response.RevenueMainCategoryRes;

public interface RevenueMainCategoryService {
    RevenueMainCategoryRes create(RevenueMainCategoryCreateReq request);

    RevenueMainCategoryRes update(Long id, RevenueMainCategoryUpdateReq request);

    RevenueMainCategoryRes getById(Long id);

    List<RevenueMainCategoryRes> list();

    void delete(Long id);
}
