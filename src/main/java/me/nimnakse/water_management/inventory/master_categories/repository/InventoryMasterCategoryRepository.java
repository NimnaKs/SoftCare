package me.nimnakse.water_management.inventory.master_categories.repository;

import me.nimnakse.water_management.inventory.master_categories.entity.InventoryMasterCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMasterCategoryRepository extends JpaRepository<InventoryMasterCategory, Long> {
    boolean existsByLevelAndNameIgnoreCaseAndParentId(Integer level, String name, Long parentId);

    boolean existsByLevelAndNameIgnoreCaseAndParentIdAndIdNot(Integer level, String name, Long parentId, Long id);

    boolean existsByParentId(Long parentId);
}
