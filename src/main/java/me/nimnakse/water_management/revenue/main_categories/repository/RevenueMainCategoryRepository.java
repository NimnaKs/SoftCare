package me.nimnakse.water_management.revenue.main_categories.repository;

import me.nimnakse.water_management.revenue.main_categories.entity.RevenueMainCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevenueMainCategoryRepository extends JpaRepository<RevenueMainCategory, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
