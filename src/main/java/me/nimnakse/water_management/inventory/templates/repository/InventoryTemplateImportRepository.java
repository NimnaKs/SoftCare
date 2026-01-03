package me.nimnakse.water_management.inventory.templates.repository;

import java.util.List;
import me.nimnakse.water_management.inventory.templates.entity.InventoryTemplateImport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTemplateImportRepository extends JpaRepository<InventoryTemplateImport, Long> {
    boolean existsByOrgUnitIdAndTemplateId(Long orgUnitId, Long templateId);

    List<InventoryTemplateImport> findByOrgUnitId(Long orgUnitId);

    Page<InventoryTemplateImport> findByOrgUnitId(Long orgUnitId, Pageable pageable);
}
