package me.nimnakse.water_management.inventory.templates.repository;

import java.util.Collection;
import java.util.List;
import me.nimnakse.water_management.inventory.templates.entity.InventoryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTemplateRepository extends JpaRepository<InventoryTemplate, Long> {
    boolean existsByTemplateCodeIgnoreCase(String templateCode);

    boolean existsByTemplateCodeIgnoreCaseAndIdNot(String templateCode, Long id);

    boolean existsByLevelOneCategoryIdAndLevelTwoCategoryIdAndLevelThreeCategoryId(
            Long levelOneCategoryId, Long levelTwoCategoryId, Long levelThreeCategoryId);

    boolean existsByLevelOneCategoryIdAndLevelTwoCategoryIdAndLevelThreeCategoryIdAndIdNot(
            Long levelOneCategoryId, Long levelTwoCategoryId, Long levelThreeCategoryId, Long id);

    List<InventoryTemplate> findByIdIn(Collection<Long> ids);
}
