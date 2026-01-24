package me.nimnakse.water_management.expenses.main_categories.repository;

import java.util.Optional;
import me.nimnakse.water_management.expenses.main_categories.entity.ExpenseMainCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExpenseMainCategoryRepository extends JpaRepository<ExpenseMainCategory, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<ExpenseMainCategory> findByNameIgnoreCase(String name);
}
