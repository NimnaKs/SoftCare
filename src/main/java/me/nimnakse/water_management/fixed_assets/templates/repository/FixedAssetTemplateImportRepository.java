package me.nimnakse.water_management.fixed_assets.templates.repository;

import java.util.List;
import me.nimnakse.water_management.fixed_assets.templates.entity.FixedAssetTemplateImport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedAssetTemplateImportRepository extends JpaRepository<FixedAssetTemplateImport, Long> {
    boolean existsByOrgUnitIdAndTemplateId(Long orgUnitId, Long templateId);

    List<FixedAssetTemplateImport> findByOrgUnitId(Long orgUnitId);

    Page<FixedAssetTemplateImport> findByOrgUnitId(Long orgUnitId, Pageable pageable);
}
