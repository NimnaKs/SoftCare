package me.nimnakse.water_management.expenses.accounts.service;

import java.util.List;
import me.nimnakse.water_management.expenses.accounts.dto.request.ExpenseAccountCreateReq;
import me.nimnakse.water_management.expenses.accounts.dto.request.ExpenseAccountUpdateReq;
import me.nimnakse.water_management.expenses.accounts.dto.response.ExpenseAccountRes;

public interface ExpenseAccountService {
    ExpenseAccountRes create(ExpenseAccountCreateReq request);

    ExpenseAccountRes update(Long id, ExpenseAccountUpdateReq request);

    ExpenseAccountRes getById(Long id);

    List<ExpenseAccountRes> list(Long mainCategoryId);

    void delete(Long id);
}
