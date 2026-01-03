package me.nimnakse.water_management.fixed_assets.templates.repository;

import java.util.Collection;
import java.util.List;
import me.nimnakse.water_management.fixed_assets.templates.entity.FixedAssetTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedAssetTemplateRepository extends JpaRepository<FixedAssetTemplate, Long> {
    boolean existsByTemplateCodeIgnoreCase(String templateCode);

    boolean existsByTemplateCodeIgnoreCaseAndIdNot(String templateCode, Long id);

    boolean existsByLevelOneCategoryIdAndLevelTwoCategoryIdAndLevelThreeCategoryId(
            Long levelOneCategoryId, Long levelTwoCategoryId, Long levelThreeCategoryId);

    boolean existsByLevelOneCategoryIdAndLevelTwoCategoryIdAndLevelThreeCategoryIdAndIdNot(
            Long levelOneCategoryId, Long levelTwoCategoryId, Long levelThreeCategoryId, Long id);

    List<FixedAssetTemplate> findByIdIn(Collection<Long> ids);

    Page<FixedAssetTemplate> findByIdNotIn(Collection<Long> ids, Pageable pageable);
}
