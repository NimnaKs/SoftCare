package me.nimnakse.water_management.fixed_assets.initial_stocks.repository;

import java.util.List;
import me.nimnakse.water_management.fixed_assets.initial_stocks.entity.FixedAssetInitialStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedAssetInitialStockRepository extends JpaRepository<FixedAssetInitialStock, Long> {
    boolean existsByOrgUnitIdAndBatchNo(Long orgUnitId, String batchNo);

    boolean existsByOrgUnitIdAndBatchNoAndIdNot(Long orgUnitId, String batchNo, Long id);

    FixedAssetInitialStock findTopByOrgUnitIdOrderByBatchSequenceDesc(Long orgUnitId);

    List<FixedAssetInitialStock> findByOrgUnitId(Long orgUnitId);

    List<FixedAssetInitialStock> findByTemplateId(Long templateId);

    List<FixedAssetInitialStock> findByOrgUnitIdAndTemplateId(Long orgUnitId, Long templateId);
}
