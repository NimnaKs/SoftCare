package me.nimnakse.water_management.liabilities.main_categories.repository;

import me.nimnakse.water_management.liabilities.main_categories.entity.LiabilityMainCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiabilityMainCategoryRepository extends JpaRepository<LiabilityMainCategory, Long> {
    boolean existsByCode(Integer code);

    boolean existsByCodeAndIdNot(Integer code, Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
