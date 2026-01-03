package me.nimnakse.water_management.inventory.templates.repository;

import java.util.Collection;
import java.util.List;
import me.nimnakse.water_management.inventory.templates.entity.InventoryTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryTemplateRepository extends JpaRepository<InventoryTemplate, Long> {
    boolean existsByTemplateCodeIgnoreCase(String templateCode);

    boolean existsByTemplateCodeIgnoreCaseAndIdNot(String templateCode, Long id);

    boolean existsByLevelOneCategoryIdAndLevelTwoCategoryIdAndLevelThreeCategoryId(
            Long levelOneCategoryId, Long levelTwoCategoryId, Long levelThreeCategoryId);

    boolean existsByLevelOneCategoryIdAndLevelTwoCategoryIdAndLevelThreeCategoryIdAndIdNot(
            Long levelOneCategoryId, Long levelTwoCategoryId, Long levelThreeCategoryId, Long id);

    List<InventoryTemplate> findByIdIn(Collection<Long> ids);

    Page<InventoryTemplate> findByIdNotIn(Collection<Long> ids, Pageable pageable);

    @Query(
            value =
                    "SELECT t FROM InventoryTemplate t "
                            + "LEFT JOIN InventoryTemplateImport i ON i.templateId = t.id "
                            + "AND i.orgUnitId = :orgUnitId AND i.deletedAt IS NULL "
                            + "WHERE t.deletedAt IS NULL AND i.id IS NULL",
            countQuery =
                    "SELECT COUNT(t) FROM InventoryTemplate t "
                            + "LEFT JOIN InventoryTemplateImport i ON i.templateId = t.id "
                            + "AND i.orgUnitId = :orgUnitId AND i.deletedAt IS NULL "
                            + "WHERE t.deletedAt IS NULL AND i.id IS NULL"
    )
    Page<InventoryTemplate> findAvailableForOrgUnit(@Param("orgUnitId") Long orgUnitId, Pageable pageable);
}
