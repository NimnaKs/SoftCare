package me.nimnakse.water_management.expenses.main_categories.repository;

import me.nimnakse.water_management.expenses.main_categories.entity.ExpenseMainCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseMainCategoryRepository extends JpaRepository<ExpenseMainCategory, Long> {
    boolean existsByCode(Integer code);

    boolean existsByCodeAndIdNot(Integer code, Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
