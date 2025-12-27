package me.nimnakse.water_management.fixed_assets.master_categories.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import me.nimnakse.water_management.fixed_assets.master_categories.entity.FixedAssetMasterCategory;

public interface FixedAssetMasterCategoryRepository extends JpaRepository<FixedAssetMasterCategory, Long> {
    boolean existsByLevelAndNameIgnoreCaseAndParentId(Integer level, String name, Long parentId);

    boolean existsByLevelAndNameIgnoreCaseAndParentIdIsNull(Integer level, String name);

    boolean existsByLevelAndNameIgnoreCaseAndParentIdAndIdNot(Integer level, String name, Long parentId, Long id);

    boolean existsByLevelAndNameIgnoreCaseAndParentIdIsNullAndIdNot(Integer level, String name, Long id);

    boolean existsByParentId(Long parentId);

    List<FixedAssetMasterCategory> findByParentId(Long parentId);
}
