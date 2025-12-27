package me.nimnakse.water_management.expenses.main_categories.service;

import java.util.List;
import me.nimnakse.water_management.expenses.main_categories.dto.request.ExpenseMainCategoryCreateReq;
import me.nimnakse.water_management.expenses.main_categories.dto.request.ExpenseMainCategoryUpdateReq;
import me.nimnakse.water_management.expenses.main_categories.dto.response.ExpenseMainCategoryRes;

public interface ExpenseMainCategoryService {
    ExpenseMainCategoryRes create(ExpenseMainCategoryCreateReq request);

    ExpenseMainCategoryRes update(Long id, ExpenseMainCategoryUpdateReq request);

    ExpenseMainCategoryRes getById(Long id);

    List<ExpenseMainCategoryRes> list();

    void delete(Long id);
}
