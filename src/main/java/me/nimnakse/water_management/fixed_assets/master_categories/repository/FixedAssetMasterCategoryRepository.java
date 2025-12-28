package me.nimnakse.water_management.fixed_assets.master_categories.repository;

import me.nimnakse.water_management.fixed_assets.master_categories.entity.FixedAssetMasterCategory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedAssetMasterCategoryRepository extends JpaRepository<FixedAssetMasterCategory, Long> {
    boolean existsByLevelAndNameIgnoreCaseAndParentId(Integer level, String name, Long parentId);

    boolean existsByLevelAndNameIgnoreCaseAndParentIdIsNull(Integer level, String name);

    boolean existsByLevelAndNameIgnoreCaseAndParentIdAndIdNot(Integer level, String name, Long parentId, Long id);

    boolean existsByLevelAndNameIgnoreCaseAndParentIdIsNullAndIdNot(Integer level, String name, Long id);

    boolean existsByParentId(Long parentId);

    List<FixedAssetMasterCategory> findByParentId(Long parentId);

    Page<FixedAssetMasterCategory> findByLevelAndParentIdIsNull(Integer level, Pageable pageable);

    Page<FixedAssetMasterCategory> findByLevelAndParentId(Integer level, Long parentId, Pageable pageable);
}
