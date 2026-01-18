package me.nimnakse.water_management.liabilities.main_categories.repository;

import java.util.Optional;
import me.nimnakse.water_management.liabilities.main_categories.entity.LiabilityMainCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LiabilityMainCategoryRepository extends JpaRepository<LiabilityMainCategory, Long> {
    boolean existsByCode(Integer code);

    boolean existsByCodeAndIdNot(Integer code, Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<LiabilityMainCategory> findByNameIgnoreCase(String name);

    @Query("select max(lmc.code) from LiabilityMainCategory lmc")
    Integer findMaxCode();
}
